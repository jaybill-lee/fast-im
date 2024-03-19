package org.jaybill.fast.im.net.spring;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jaybill.fast.im.net.ChannelErrorHandler;
import org.jaybill.fast.im.net.HttpMultiProtocolUpgradeHandler;
import org.jaybill.fast.im.net.http.HttpConfig;
import org.jaybill.fast.im.net.http.HttpDispatcher;
import org.jaybill.fast.im.net.spring.properties.ConnectorProperties;
import org.jaybill.fast.im.net.spring.properties.WebSocketProperties;
import org.jaybill.fast.im.net.ws.WebSocketFrameHandler;
import org.jaybill.fast.im.net.ws.WebSocketListener;
import org.jaybill.fast.im.net.ws.WebSocketUpgradeConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@EnableConfigurationProperties(value = { ConnectorProperties.class, WebSocketProperties.class })
public class NettyBeanConfig implements ApplicationListener<ContextRefreshedEvent> {

    public static final String BEAN_NAME = "httpServerBootStrap";

    @Bean(name = BEAN_NAME)
    public ServerBootstrap httpServerBootStrap(WebSocketProperties wsProperties,
            WebSocketListener webSocketListener, @Autowired(required = false) HttpFiltersFactory httpFiltersFactory) {
        var bootstrap = new ServerBootstrap();
        var bossGroup = new NioEventLoopGroup(1);
        var workerGroup = new NioEventLoopGroup();

        bootstrap.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) {
                socketChannel.pipeline()
                    .addLast(new HttpMultiProtocolUpgradeHandler(
                        // WebSocket
                        getWebSocketUpgradeConfig(wsProperties, webSocketListener),
                        // HTTP
                        new HttpConfig(getDispatcher(httpFiltersFactory))))
                    .addLast(new ChannelErrorHandler());
                }
            });
        return bootstrap;
    }

    /**
     * All bean init success, then we start net server.
     * Note: After the method is executed, external traffic can enter.
     *       So we must ensure that the resource initialization is completed before this.
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        var ctx = (ConfigurableApplicationContext) event.getApplicationContext();
        var config = ctx.getBean(ConnectorProperties.class);
        var httpServerBootstrap = ctx.getBean(BEAN_NAME, ServerBootstrap.class);
        var httpPort = config.getHttpPort();
        try {
            httpServerBootstrap.bind(httpPort).sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("fast-im-server start error:", e);
            throw new ServerStartException("be interrupt");
        }
        log.info("fast-im-server started, httpPort:{}", httpPort);
        ctx.addApplicationListener((ApplicationListener<ContextClosedEvent>) evt -> {
            log.info("fast-im-server stopping");
            httpServerBootstrap.config().group().shutdownGracefully().syncUninterruptibly();
            httpServerBootstrap.config().childGroup().shutdownGracefully().syncUninterruptibly();
            log.info("fast-im-server stopped");
        });
    }

    private WebSocketUpgradeConfig getWebSocketUpgradeConfig(
            WebSocketProperties wsProperties, WebSocketListener webSocketListener) {
        if (webSocketListener == null) {
            return null;
        }
        if (wsProperties == null) {
            wsProperties = new WebSocketProperties();
            wsProperties.setIdleSeconds(30);
            wsProperties.setPath("/ws");
        }
        return WebSocketUpgradeConfig.builder()
                .path(wsProperties.getPath())
                .handlers(List.of(
                        new IdleStateHandler(0, 0, wsProperties.getIdleSeconds(), TimeUnit.SECONDS),
                        new WebSocketFrameHandler(webSocketListener)))
                .listener(webSocketListener)
                .build();
    }

    private HttpDispatcher getDispatcher(HttpFiltersFactory httpFiltersFactory) {
        if (httpFiltersFactory == null || CollectionUtils.isEmpty(httpFiltersFactory.getFilters())) {
            return new HttpDispatcher();
        }
        var dispatcher = new HttpDispatcher();
        httpFiltersFactory.getFilters().forEach(dispatcher::addFilter);
        return dispatcher;
    }
}
