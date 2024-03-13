package org.jaybill.fast.im.connector.beans;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.jaybill.fast.im.connector.ws.DefaultWebSocketListener;
import org.jaybill.fast.im.connector.ws.ChannelEvtHandler;
import org.jaybill.fast.im.net.ChannelErrorHandler;
import org.jaybill.fast.im.net.HttpMultiProtocolUpgradeHandler;
import org.jaybill.fast.im.net.http.HttpConfig;
import org.jaybill.fast.im.net.http.HttpDispatcher;
import org.jaybill.fast.im.net.ws.WebSocketFrameHandler;
import org.jaybill.fast.im.net.ws.WebSocketUpgradeConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@Configuration
public class NettyBeanConfig {

    @Bean(name = BeanName.HTTP_SERVER_BOOTSTRAP)
    public ServerBootstrap httpServerBootStrap(ThreadFactory listenerThreadFactory, ChannelEvtHandler channelEvtHandler) {
        var bootstrap = new ServerBootstrap();
        var bossGroup = new NioEventLoopGroup(1);
        var workerGroup = new NioEventLoopGroup();

        var defaultWebSocketListener = new DefaultWebSocketListener(channelEvtHandler);
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        socketChannel.pipeline()
                                .addLast(new HttpMultiProtocolUpgradeHandler(
                                        WebSocketUpgradeConfig.builder()
                                                .path("/ws")
                                                .handlers(List.of(
                                                        new IdleStateHandler(0, 0, 30, TimeUnit.SECONDS),
                                                        new WebSocketFrameHandler(defaultWebSocketListener, listenerThreadFactory)))
                                                .listener(defaultWebSocketListener)
                                                .build(),
                                        HttpConfig.builder()
                                                .dispatcher(new HttpDispatcher())
                                                .build(),
                                        listenerThreadFactory))
                                .addLast(new ChannelErrorHandler());
                    }
                });
        return bootstrap;
    }
}
