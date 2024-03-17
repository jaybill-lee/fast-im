package org.jaybill.fast.im.connector.beans;

import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.config.TlsConfig;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class HttpClientConfig {

    @Bean
    public CloseableHttpAsyncClient httpAsyncClient() {
        CloseableHttpAsyncClient client = HttpAsyncClients.custom()
                .setIOReactorConfig(IOReactorConfig.custom()
                        .setSoTimeout(Timeout.ofSeconds(30))
                        .setIoThreadCount(Runtime.getRuntime().availableProcessors())
                        .setSoReuseAddress(true)
                        .setTcpNoDelay(true)
                        .setSoLinger(Timeout.ofSeconds(0))
                        .build())
                .setConnectionManager(PoolingAsyncClientConnectionManagerBuilder.create()
                        .setDefaultConnectionConfig(ConnectionConfig.custom()
                                .setConnectTimeout(Timeout.ofSeconds(10))
                                .setSocketTimeout(Timeout.ofSeconds(30))
                                .setTimeToLive(Timeout.ofMinutes(30))
                                .setValidateAfterInactivity(Timeout.ofMinutes(2))
                                .build())
                        .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT)
                        .setConnPoolPolicy(PoolReusePolicy.FIFO)
                        .setDnsResolver(new SystemDefaultDnsResolver())
                        .setMaxConnPerRoute(200)
                        .setMaxConnTotal(2000)
                        .setDefaultTlsConfig(TlsConfig.custom()
                                .setHandshakeTimeout(Timeout.ofSeconds(10))
                                .setVersionPolicy(HttpVersionPolicy.NEGOTIATE)
                                .build())
                        .build())
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectionRequestTimeout(Timeout.ofSeconds(10))
                        .setResponseTimeout(Timeout.ofSeconds(30))
                        .setCircularRedirectsAllowed(false)
                        .setRedirectsEnabled(true)
                        .setMaxRedirects(3)
                        .setDefaultKeepAlive(2, TimeUnit.MINUTES)
                        .setHardCancellationEnabled(false)
                        .build())
                .evictExpiredConnections()
                .evictIdleConnections(Timeout.ofSeconds(10))
                .setRetryStrategy(new DefaultHttpRequestRetryStrategy(3, Timeout.ofMilliseconds(50)))
                .addRequestInterceptorFirst((request, entity, context) -> {})
                .addResponseInterceptorFirst((response, entity, context) -> {})
                .build();
        client.start();
        return client;
    }
}
