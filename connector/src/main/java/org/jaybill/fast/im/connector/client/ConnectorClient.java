package org.jaybill.fast.im.connector.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Method;
import org.jaybill.fast.im.common.util.JsonUtil;
import org.jaybill.fast.im.common.web.util.JwtParseUtil;
import org.jaybill.fast.im.connector.util.JwtUtil;
import org.jaybill.fast.im.connector.ws.PushResult;
import org.jaybill.fast.im.connector.ws.evt.InternalPushEvt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class ConnectorClient {

    @Value("${fast.im.connector.connection.service.schema:http}")
    private String schema;
    @Value("${fast.im.connector.connection.service.port:8080}")
    private int port;

    @Autowired
    private CloseableHttpAsyncClient httpAsyncClient;

    /**
     * Request a specific server that will search for a local channel that meets the requirements,
     * and push the event out.
     */
    public CompletableFuture<PushResult> localPush(String serverAddress, InternalPushEvt pushEvt) {
        var future = new CompletableFuture<PushResult>();
        var req = SimpleHttpRequest.create(Method.POST, UriBuilder.builder()
                .schema(schema)
                .address(serverAddress)
                .path("/local/push")
                .build());
        req.setHeader(JwtParseUtil.AUTHORIZATION, JwtParseUtil.BEARER + JwtUtil.createInternalServiceToken());
        req.setBody(JsonUtil.toJson(pushEvt), ContentType.APPLICATION_JSON);
        var ctx = new HttpClientContext();
        httpAsyncClient.execute(req, new DefaultFutureCallback<>(req, ctx, future) {
            @Override
            public void whenSuccess(SimpleHttpResponse result) {
                var body = result.getBody().getBodyText();
                var ack = JsonUtil.fromJson(body, PushResult.class);
                future.complete(ack);
            }
        });
        return future;
    }
}
