package org.jaybill.fast.im.net.http;

import io.netty.buffer.ByteBuf;

import java.util.Map;

public interface BaseHttpRequest {

    Map<String, String> headers();

    String header(String name);

    String param(String name);

    String version();

    String method();

    String path();

    ByteBuf body();
}
