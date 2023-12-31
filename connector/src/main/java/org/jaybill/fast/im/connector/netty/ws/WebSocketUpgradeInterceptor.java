package org.jaybill.fast.im.connector.netty.ws;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMessage;

import java.util.concurrent.CompletableFuture;

public interface WebSocketUpgradeInterceptor {
    CompletableFuture<?> before(ChannelHandlerContext ctx, HttpMessage req);

    default void after(ChannelHandlerContext ctx) {}
}
