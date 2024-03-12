package org.jaybill.fast.im.net.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import org.jaybill.fast.im.net.util.UriUtil;

import java.util.HashMap;
import java.util.Map;

public class Http1Request implements BaseHttpRequest {

    private FullHttpRequest originalReq;

    private final String path;
    private final Map<String, String> params;
    private final Map<String, String> headers;
    private final ByteBuf body;

    public Http1Request(FullHttpRequest originalReq) {
        this.originalReq = originalReq;
        this.headers = new HashMap<>();
        var originalHeaders = this.originalReq.headers();
        originalHeaders.forEach(header -> this.headers.put(header.getKey(), header.getValue()));
        var uriComponent = UriUtil.parseUri(originalReq.uri());
        this.path = uriComponent.getPath();
        this.params = uriComponent.getParams();
        this.body = originalReq.content();
    }

    @Override
    public Map<String, String> headers() {
        return this.headers;
    }

    @Override
    public String header(String name) {
        return this.headers.get(name);
    }

    @Override
    public String param(String name) {
        return this.params.get(name);
    }

    @Override
    public String version() {
        return this.originalReq.protocolVersion().protocolName();
    }

    @Override
    public String method() {
        return this.originalReq.method().name().toUpperCase();
    }

    @Override
    public String path() {
        return this.path;
    }

    @Override
    public ByteBuf body() {
        return this.body;
    }

    public static Http1Request adapt(FullHttpRequest req) {
        return new Http1Request(req);
    }
}
