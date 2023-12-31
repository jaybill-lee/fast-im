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
import org.jaybill.fast.im.connector.netty.http.*;
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
    private final HttpConfig httpConfig;

    public HttpMultiProtocolUpgradeHandler(WebSocketUpgradeConfig wsConfig, HttpConfig httpConfig) {
        AssertUtil.notNull(wsConfig);
        this.wsConfig = wsConfig;
        if (!this.wsConfig.getPath().startsWith("/")) {
            this.wsConfig.setPath("/" + this.wsConfig.getPath());
        }
        this.httpConfig = httpConfig;
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
        } else if (verifyUpgradeH2cHeaders(req)) {
            // Try to handle HTTP2 (h2c) handshake
            if (!this.upgradeH2c(ctx, req)) {
                // Http 1.1
                this.httpConfig.getDispatcher().service(Http1Request.adapt(req), Http1Response.adapt(ctx));
                ctx.fireChannelRead(req.retain());
            }
        } else {
            // Http 1.1
            this.httpConfig.getDispatcher().service(Http1Request.adapt(req), Http1Response.adapt(ctx));
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
            interceptor.before(ctx, req).whenComplete((r, e) -> {
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

    private boolean verifyUpgradeH2cHeaders(FullHttpRequest request) {
        List<CharSequence> requestedProtocols = splitHeader(request.headers().get(HttpHeaderNames.UPGRADE));
        CharSequence upgradeProtocol = null;
        for (CharSequence p : requestedProtocols) {
            if (AsciiString.contentEquals(Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME, p)) {
                upgradeProtocol = p;
                break;
            }
        }
        if (upgradeProtocol == null) {
            // None of the requested protocols are supported, don't upgrade.
            return false;
        }

        // Make sure the CONNECTION header is present.
        List<String> connectionHeaderValues = request.headers().getAll(HttpHeaderNames.CONNECTION);
        if (CollectionUtils.isEmpty(connectionHeaderValues)) {
            return false;
        }
        String concatenatedConnectionValue = String.join(String.valueOf(COMMA), connectionHeaderValues);

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

        return true;
    }

    // After upgrade successfully, the pipeline would be:
    // Head -> WebSocketFrameEncoder -> WebSocketFrameDecoder -> WebSocketFrameHandler -> ChannelErrorHandler -> Tail
    private void upgradeWebsocket0(ChannelHandlerContext ctx, FullHttpRequest req) {
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
                TcpUtil.rst(ctx);
            }
        });
    }

    // After upgrade successfully, the pipeline would be:
    // Head -> Http2ConnectionHandler -> ChannelErrorHandler -> Tail
    private boolean upgradeH2c(final ChannelHandlerContext ctx, final FullHttpRequest request) {
        CharSequence upgradeProtocol = Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME;
        HttpServerUpgradeHandler.UpgradeCodec upgradeCodec = new Http2ServerUpgradeCodec(
                new DefaultHttp2ConnectionHandlerBuilder(httpConfig.getDispatcher()).build());

        // Prepare and send the upgrade response. Wait for this write to complete before upgrading,
        // since we need the old codec in-place to properly encode the response.
        final FullHttpResponse upgradeResponse = createUpgradeResponse(upgradeProtocol);
        if (!upgradeCodec.prepareUpgradeResponse(ctx, request, upgradeResponse.headers())) {
            return false;
        }

        // After writing the upgrade response we immediately prepare the
        // pipeline for the next protocol to avoid a race between completion
        // of the write future and receiving data before the pipeline is
        // restructured.
        this.upgrading = true;
        final ChannelFuture writeComplete = ctx.writeAndFlush(upgradeResponse);
        // Perform the upgrade to the new protocol.
        ctx.pipeline().remove(HttpServerCodec.class);
        upgradeCodec.upgradeTo(ctx, request);
        ctx.pipeline().remove(this);
        ctx.pipeline().remove(HttpObjectAggregator.class);

        // Notify the listener.
        // Then listener can send the response of the handshake request through HTTP2.
        ctx.fireUserEventTriggered(new H2cUpgradeEvt(upgradeProtocol, request));

        // Add the listener last to avoid firing upgrade logic after
        // the channel is already closed since the listener may fire
        // immediately if to write failed eagerly.
        writeComplete.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        return true;
    }

    private String getWebSocketLocation(FullHttpRequest req, String path) {
        String host = req.headers().get(HttpHeaderNames.HOST);
        return "ws://" + host + path;
    }

    private static List<CharSequence> splitHeader(CharSequence header) {
        if (header == null) {
            return Collections.emptyList();
        }
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
        if (!builder.isEmpty()) {
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
