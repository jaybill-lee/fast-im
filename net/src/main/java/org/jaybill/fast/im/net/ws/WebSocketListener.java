package org.jaybill.fast.im.net.ws;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMessage;

public interface WebSocketListener {
    boolean beforeHandshake(ChannelHandlerContext ctx, HttpMessage req);

    void onHandshakeSuccess(ChannelHandlerContext ctx);

    void onHandshakeFail(ChannelHandlerContext ctx);

    void onChannelHeartbeat(ChannelHandlerContext ctx);

    void onChannelTextFrame(ChannelHandlerContext ctx, String text);

    void onChannelClose(ChannelHandlerContext ctx);
}
