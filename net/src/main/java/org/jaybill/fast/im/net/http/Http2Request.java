package org.jaybill.fast.im.net.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http2.Http2Headers;
import org.jaybill.fast.im.net.util.UriUtil;

import java.util.HashMap;
import java.util.Map;

public class Http2Request implements BaseHttpRequest {

    private final int streamId;
    private final int padding;
    private final Http2Headers http2Headers;
    private final ByteBuf body;

    private final Map<String, String> headers;
    private final Map<String, String> paramsMap;
    private final String path;

    private Http2Request(int streamId, Http2Headers http2Headers, ByteBuf body, int padding) {
        this.http2Headers = http2Headers;
        this.body = body;
        this.streamId = streamId;
        this.padding = padding;
        this.headers = new HashMap<>();
        for (var entry : http2Headers) {
            headers.put(entry.getKey().toString(), entry.getValue().toString());
        }
        var rawPath = http2Headers.path().toString();
        var uriComponent = UriUtil.parseUri(rawPath);
        this.path = uriComponent.getPath();
        this.paramsMap = uriComponent.getParams();
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
        return paramsMap.get(name);
    }

    @Override
    public String version() {
        return http2Headers.scheme().toString();
    }

    @Override
    public String method() {
        return http2Headers.method().toString().toUpperCase();
    }

    @Override
    public String path() {
        return this.path;
    }

    @Override
    public ByteBuf body() {
        return body;
    }

    public static Http2Request adapt(int streamId, Http2Headers headers, ByteBuf data, int padding) {
        return new Http2Request(streamId, headers, data, padding);
    }
}
