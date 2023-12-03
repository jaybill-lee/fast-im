package org.jaybill.fast.im.connector.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.Method;
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

    public CompletableFuture<?> asyncDiscardConn(String serverIp, String bizId, String userId, String platform) {
        if (StringUtils.isAnyEmpty(serverIp, bizId, userId, platform)) {
            return CompletableFuture.completedFuture(null);
        }

        var future = new CompletableFuture<>();
        var req = SimpleHttpRequest.create(Method.PUT, UriBuilder.builder()
                .schema(schema)
                .address(serverIp)
                .port(port)
                .path("/discard")
                .params("bizId", bizId)
                .params("userId", userId)
                .params("platform", platform)
                .build());
        var ctx = new HttpClientContext();
        httpAsyncClient.execute(req, new DefaultFutureCallback<>(req, ctx, future) {
            @Override
            public void whenSuccess(SimpleHttpResponse result) {

            }
        });
        return future;
    }
}
