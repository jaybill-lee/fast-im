package org.jaybill.fast.im.net.ws;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.jaybill.fast.im.net.util.TcpUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Slf4j
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private final WebSocketListener webSocketListener;
    private final Executor executor;

    public WebSocketFrameHandler(WebSocketListener webSocketListener, ThreadFactory threadFactory) {
        this.webSocketListener = webSocketListener;
        this.executor = Executors.newThreadPerTaskExecutor(threadFactory);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) {
        // PING
        if (msg instanceof PingWebSocketFrame) {
            CompletableFuture
                    .runAsync(() -> webSocketListener.onChannelHeartbeat(ctx), executor)
                    .whenComplete((r, e) -> {
                        if (e == null) {
                            return;
                        }
                        log.error("on channel heartbeat error:", e);
                    });
        }

        // TEXT
        if (msg instanceof TextWebSocketFrame textMsg) {
            var textFrame = textMsg.text();
            log.debug("text frame: {}", textFrame);
            CompletableFuture
                    .runAsync(() -> webSocketListener.onChannelTextFrame(ctx, textFrame), executor)
                    .whenComplete((r, e) -> {
                        if (e == null) {
                            return;
                        }
                        log.error("on channel text frame error:", e);
                    });
        }
    }

    /**
     * handle heartbeat lose
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (!(evt instanceof IdleStateEvent)) {
            return;
        }

        log.error("channel lost heartbeat, force close. longText:{}", ctx.channel().id().asLongText());
        TcpUtil.rst(ctx);
    }
}
