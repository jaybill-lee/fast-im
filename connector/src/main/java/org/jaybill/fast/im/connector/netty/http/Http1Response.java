package org.jaybill.fast.im.connector.netty.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class Http1Response implements BaseHttpResponse {

    private final ChannelHandlerContext ctx;
    private boolean responded = false;

    public Http1Response(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public boolean responded() {
        return responded;
    }

    @Override
    public void response(HttpResponseStatus status, Map<String, String> headers, ByteBuf body) {
        if (responded) {
            return;
        }
        if (body == null) {
            body = Unpooled.EMPTY_BUFFER;
        }
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, body);
        response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
        ctx.writeAndFlush(response);
        responded = true;
    }

    public static Http1Response adapt(ChannelHandlerContext ctx) {
        return new Http1Response(ctx);
    }
}
