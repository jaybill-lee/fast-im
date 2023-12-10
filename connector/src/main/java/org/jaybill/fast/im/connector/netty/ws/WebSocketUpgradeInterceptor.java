package org.jaybill.fast.im.connector.netty.ws;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.concurrent.CompletableFuture;

public interface WebSocketUpgradeInterceptor {
    CompletableFuture<?> before(ChannelHandlerContext ctx, FullHttpRequest req);

    default void after(ChannelHandlerContext ctx) {}
}
