package org.jaybill.fast.im.connector.beans;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.jaybill.fast.im.connector.netty.ChannelErrorHandler;
import org.jaybill.fast.im.connector.netty.HttpMultiProtocolUpgradeHandler;
import org.jaybill.fast.im.connector.netty.http.HttpConfig;
import org.jaybill.fast.im.connector.netty.http.HttpDispatcher;
import org.jaybill.fast.im.connector.netty.ws.WebSocketFrameHandler;
import org.jaybill.fast.im.connector.netty.ws.WebSocketUpgradeConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.ThreadFactory;

@Configuration
public class NettyBeanConfig {

    @Bean(name = BeanName.HTTP_SERVER_BOOTSTRAP)
    public ServerBootstrap httpServerBootStrap(ThreadFactory listenerThreadFactory) {
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
                                        WebSocketUpgradeConfig.builder()
                                                .path("/ws")
                                                .handlers(List.of(new WebSocketFrameHandler(listenerThreadFactory)))
                                                .build(),
                                        HttpConfig.builder()
                                                .dispatcher(new HttpDispatcher())
                                                .build()))
                                .addLast(new ChannelErrorHandler());
                    }
                });
        return bootstrap;
    }
}
