package org.jaybill.fast.im.connector.netty;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketDecoderConfig;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2ServerUpgradeCodec;
import io.netty.util.AsciiString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jaybill.fast.im.common.util.AssertUtil;
import org.jaybill.fast.im.connector.netty.http.H2cUpgradeEvt;
import org.jaybill.fast.im.connector.netty.http.HelloWorldHttp2HandlerBuilder;
import org.jaybill.fast.im.connector.netty.ws.WebSocketUpgradeConfig;
import org.jaybill.fast.im.connector.util.TcpUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static io.netty.handler.codec.http.HttpResponseStatus.SWITCHING_PROTOCOLS;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.http2.Http2CodecUtil.HTTP_UPGRADE_SETTINGS_HEADER;
import static io.netty.util.AsciiString.containsAllContentEqualsIgnoreCase;
import static io.netty.util.AsciiString.containsContentEqualsIgnoreCase;
import static io.netty.util.internal.StringUtil.COMMA;

@Slf4j
public class HttpMultiProtocolUpgradeHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private boolean upgrading;
    private final WebSocketUpgradeConfig wsConfig;

    public HttpMultiProtocolUpgradeHandler(WebSocketUpgradeConfig wsConfig) {
        AssertUtil.notNull(wsConfig);
        this.wsConfig = wsConfig;
        if (!this.wsConfig.getPath().startsWith("/")) {
            this.wsConfig.setPath("/" + this.wsConfig.getPath());
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        ctx.pipeline()
                .addFirst(new HttpObjectAggregator(65535))
                .addFirst(new HttpServerCodec());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) {
        if (upgrading) {
            log.error("connection is in upgrading, id:{}", ctx.channel().id());
            TcpUtil.rst(ctx);
            return;
        }

        if (req.uri().equals(wsConfig.getPath())) {
            // Try to handle websocket handshake.
            this.upgradeWebsocket(ctx, req);
        } else if (req.headers().contains(HttpHeaderNames.UPGRADE,
                Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME, true)) {
            // Try to handle HTTP2 (h2c) handshake
            if (!this.upgradeH2c(ctx, req)) {
                ctx.fireChannelRead(req.retain());
            }
        } else {
            ctx.fireChannelRead(req.retain());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof WebSocketHandshakeException) {
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.wrappedBuffer(cause.getMessage().getBytes()));
            ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            ctx.fireExceptionCaught(cause);
            ctx.close();
        }
    }

    private void upgradeWebsocket(ChannelHandlerContext ctx, FullHttpRequest req) {
        var interceptor = wsConfig.getInterceptor();
        if (wsConfig.getInterceptor() != null) {
            // We need to retain the msg, because the whenComplete() will be async exec,
            // then the super-class will release the msg if we not retain the msg.
            req.retain();
            // copy the FullHttpRequest to a new ByteBuf, prevent that memory leak.
            var copyReq = req.copy();
            interceptor.before(ctx, copyReq).whenComplete((r, e) -> {
                copyReq.release(); // remember to release it
                if (e != null) {
                    TcpUtil.rst(ctx);
                } else {
                    this.upgradeWebsocket0(ctx, req);
                }
            }).whenComplete((r, e) -> {
                if (e != null) {
                    log.error("before upgrade to websocket handle err:", e);
                }
            });
        } else {
            this.upgradeWebsocket0(ctx, req);
        }
    }

    private void upgradeWebsocket0(ChannelHandlerContext ctx, FullHttpRequest req) {
        // After upgrade successfully, the pipeline would be:
        // Head -> WebSocketFrameEncoder -> WebSocketFrameDecoder -> WebSocketFrameHandler -> ChannelErrorHandler -> Tail
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                getWebSocketLocation(req, wsConfig.getPath()),
                null,
                WebSocketDecoderConfig.newBuilder()
                        .maxFramePayloadLength(65536)
                        .allowMaskMismatch(false)
                        .allowExtensions(true)
                        .build()
        );
        var handshaker = wsFactory.newHandshaker(req);
        this.upgrading = true;
        handshaker.handshake(ctx.channel(), req).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.debug("WebSocket handshake success");
                if (CollectionUtils.isNotEmpty(wsConfig.getHandlers())) {
                    ctx.pipeline().addLast(wsConfig.getHandlers().toArray(ChannelHandler[]::new));
                }
                ctx.pipeline().remove(this);
            } else {
                log.debug("WebSocket handshake fail, e:", future.cause());
            }
        });
    }

    private boolean upgradeH2c(final ChannelHandlerContext ctx, final FullHttpRequest request) {
        // After upgrade successfully, the pipeline would be:
        // Head -> Http2ConnectionHandler -> ChannelErrorHandler -> Tail
        List<CharSequence> requestedProtocols = splitHeader(request.headers().get(HttpHeaderNames.UPGRADE));
        HttpServerUpgradeHandler.UpgradeCodec upgradeCodec = null;
        CharSequence upgradeProtocol = null;
        for (CharSequence p : requestedProtocols) {
            if (AsciiString.contentEquals(Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME, p)) {
                upgradeCodec = new Http2ServerUpgradeCodec(new HelloWorldHttp2HandlerBuilder().build());
                upgradeProtocol = p;
                break;
            }
        }
        if (upgradeCodec == null) {
            // None of the requested protocols are supported, don't upgrade.
            return false;
        }

        // Make sure the CONNECTION header is present.
        List<String> connectionHeaderValues = request.headers().getAll(HttpHeaderNames.CONNECTION);
        if (connectionHeaderValues == null || connectionHeaderValues.isEmpty()) {
            return false;
        }

        StringBuilder concatenatedConnectionValue = new StringBuilder(connectionHeaderValues.size() * 10);
        for (CharSequence connectionHeaderValue : connectionHeaderValues) {
            concatenatedConnectionValue.append(connectionHeaderValue).append(COMMA);
        }
        concatenatedConnectionValue.setLength(concatenatedConnectionValue.length() - 1);

        // Make sure the CONNECTION header contains UPGRADE as well as all protocol-specific headers.
        Collection<CharSequence> requiredHeaders = Collections.singletonList(HTTP_UPGRADE_SETTINGS_HEADER);
        List<CharSequence> values = splitHeader(concatenatedConnectionValue);
        if (!containsContentEqualsIgnoreCase(values, HttpHeaderNames.UPGRADE) ||
                !containsAllContentEqualsIgnoreCase(values, requiredHeaders)) {
            return false;
        }

        // Ensure that all required protocol-specific headers are found in the request.
        for (CharSequence requiredHeader : requiredHeaders) {
            if (!request.headers().contains(requiredHeader)) {
                return false;
            }
        }

        // Prepare and send the upgrade response. Wait for this write to complete before upgrading,
        // since we need the old codec in-place to properly encode the response.
        final FullHttpResponse upgradeResponse = createUpgradeResponse(upgradeProtocol);
        if (!upgradeCodec.prepareUpgradeResponse(ctx, request, upgradeResponse.headers())) {
            return false;
        }

        final H2cUpgradeEvt event = new H2cUpgradeEvt(upgradeProtocol, request);
        // After writing the upgrade response we immediately prepare the
        // pipeline for the next protocol to avoid a race between completion
        // of the write future and receiving data before the pipeline is
        // restructured.
        try {
            this.upgrading = true;
            final ChannelFuture writeComplete = ctx.writeAndFlush(upgradeResponse);
            // Perform the upgrade to the new protocol.
            ctx.pipeline().remove(HttpServerCodec.class);
            upgradeCodec.upgradeTo(ctx, request);
            ctx.pipeline().remove(this);
            ctx.pipeline().remove(HttpObjectAggregator.class);

            ctx.fireUserEventTriggered(event);

            // Add the listener last to avoid firing upgrade logic after
            // the channel is already closed since the listener may fire
            // immediately if the write failed eagerly.
            writeComplete.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        } finally {
            // Release the event if the upgrade event wasn't fired.
        }
        return true;
    }

    private String getWebSocketLocation(FullHttpRequest req, String path) {
        String host = req.headers().get(HttpHeaderNames.HOST);
        return "ws://" + host + path;
    }

    private static List<CharSequence> splitHeader(CharSequence header) {
        final StringBuilder builder = new StringBuilder(header.length());
        final List<CharSequence> protocols = new ArrayList<CharSequence>(4);
        for (int i = 0; i < header.length(); ++i) {
            char c = header.charAt(i);
            if (Character.isWhitespace(c)) {
                // Don't include any whitespace.
                continue;
            }
            if (c == ',') {
                // Add the string and reset the builder for the next protocol.
                protocols.add(builder.toString());
                builder.setLength(0);
            } else {
                builder.append(c);
            }
        }

        // Add the last protocol
        if (builder.length() > 0) {
            protocols.add(builder.toString());
        }

        return protocols;
    }

    private FullHttpResponse createUpgradeResponse(CharSequence upgradeProtocol) {
        DefaultFullHttpResponse res = new DefaultFullHttpResponse(
                HTTP_1_1, SWITCHING_PROTOCOLS, Unpooled.EMPTY_BUFFER, true);
        res.headers().add(HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE);
        res.headers().add(HttpHeaderNames.UPGRADE, upgradeProtocol);
        return res;
    }
}
