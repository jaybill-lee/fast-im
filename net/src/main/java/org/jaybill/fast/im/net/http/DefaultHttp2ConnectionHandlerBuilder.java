/*
 * Copyright 2015 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.jaybill.fast.im.net.http;

import io.netty.handler.codec.http2.*;

import static io.netty.handler.logging.LogLevel.INFO;

public final class DefaultHttp2ConnectionHandlerBuilder
        extends AbstractHttp2ConnectionHandlerBuilder<DefaultHttp2ConnectionHandler, DefaultHttp2ConnectionHandlerBuilder> {

    private static final Http2FrameLogger logger = new Http2FrameLogger(INFO, DefaultHttp2ConnectionHandler.class);

    private final HttpDispatcher dispatcher;

    public DefaultHttp2ConnectionHandlerBuilder(HttpDispatcher dispatcher) {
        frameLogger(logger);
        this.dispatcher = dispatcher;
    }

    @Override
    public DefaultHttp2ConnectionHandler build() {
        return super.build();
    }

    @Override
    protected DefaultHttp2ConnectionHandler build(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder,
                                                  Http2Settings initialSettings) {
        DefaultHttp2ConnectionHandler handler = new DefaultHttp2ConnectionHandler(decoder, encoder, initialSettings, dispatcher);
        frameListener(handler);
        return handler;
    }
}
