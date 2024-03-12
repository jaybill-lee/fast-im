package org.jaybill.fast.im.net.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Map;

public interface BaseHttpResponse {

    boolean responded();

    void response(HttpResponseStatus status, Map<String, String> headers, ByteBuf body);
}
