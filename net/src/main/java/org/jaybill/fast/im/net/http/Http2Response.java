package org.jaybill.fast.im.net.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class Http2Response implements BaseHttpResponse {

    private final ChannelHandlerContext ctx;
    private final Http2ConnectionHandler handler;
    private final int streamId;
    private final Http2ConnectionEncoder encoder;
    private boolean responded = false;

    public Http2Response(Http2ConnectionHandler handler, ChannelHandlerContext ctx, int streamId, Http2ConnectionEncoder encoder) {
        this.handler = handler;
        this.ctx = ctx;
        this.streamId = streamId;
        this.encoder = encoder;
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
        var http2Headers = new DefaultHttp2Headers().status(status.codeAsText());
        if (headers != null) {
            headers.forEach(http2Headers::set);
        }
        if (body != null) {
            encoder.writeHeaders(ctx, streamId, http2Headers, 0, false, ctx.newPromise());
            encoder.writeData(ctx, streamId, body, 0, true, ctx.newPromise());
            handler.flush(ctx);
        } else {
            encoder.writeHeaders(ctx, streamId, http2Headers, 0, true, ctx.newPromise());
        }
        responded = true;
    }

    public static Http2Response adapt(Http2ConnectionHandler handler, ChannelHandlerContext ctx, int streamId, Http2ConnectionEncoder encoder) {
        return new Http2Response(handler, ctx, streamId, encoder);
    }
}
