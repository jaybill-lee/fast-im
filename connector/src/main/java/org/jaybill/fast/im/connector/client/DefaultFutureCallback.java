package org.jaybill.fast.im.connector.client;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.concurrent.FutureCallback;

import java.util.concurrent.CompletableFuture;

public abstract class DefaultFutureCallback<T> implements FutureCallback<SimpleHttpResponse> {

    private SimpleHttpRequest req;
    private HttpClientContext ctx;
    private CompletableFuture<T> future;
    private long startTime;

    public DefaultFutureCallback(SimpleHttpRequest req, HttpClientContext ctx, CompletableFuture<T> future) {
        this.req = req;
        this.ctx = ctx;
        this.future = future;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public void completed(SimpleHttpResponse result) {

    }

    @Override
    public void failed(Exception ex) {

    }

    @Override
    public void cancelled() {

    }

    /**
     * receive response, and response code = 200.
     * @param result
     */
    public abstract void whenSuccess(SimpleHttpResponse result);
}
