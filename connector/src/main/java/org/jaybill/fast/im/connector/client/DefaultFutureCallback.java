package org.jaybill.fast.im.connector.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.concurrent.FutureCallback;

import java.util.concurrent.CompletableFuture;

@Slf4j
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
        if (result.getCode() == 200) {
            this.whenSuccess(result);
            return;
        }
        log.error("http status code:{}", result.getCode());
        this.future.completeExceptionally(new IllegalStateException("HTTP status != 200"));
    }

    @Override
    public void failed(Exception ex) {
        this.future.completeExceptionally(ex);
    }

    @Override
    public void cancelled() {
        this.future.cancel(true);
    }

    /**
     * receive response, and response code = 200.
     * @param result
     */
    public abstract void whenSuccess(SimpleHttpResponse result);
}
