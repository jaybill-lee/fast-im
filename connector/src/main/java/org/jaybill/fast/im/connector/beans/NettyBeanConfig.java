package org.jaybill.fast.im.connector.beans;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import org.jaybill.fast.im.connector.listener.ConnectionListener;
import org.jaybill.fast.im.connector.netty.WebSocketErrorHandler;
import org.jaybill.fast.im.connector.netty.WebSocketFrameHandler;
import org.jaybill.fast.im.connector.netty.WebSocketUpgradeHandler;
import org.jaybill.fast.im.connector.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ThreadFactory;

@Configuration
public class NettyBeanConfig {

    @Bean(name = BeanName.HTTP_SERVER_BOOTSTRAP)
    public ServerBootstrap httpServerBootStrap(ConnectionListener listener,
                                               ThreadFactory listenerThreadFactory) {
        var bootstrap = new ServerBootstrap();
        var bossGroup = new NioEventLoopGroup(1);
        var workerGroup = new NioEventLoopGroup();

        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        socketChannel.pipeline()
                                .addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(65536))
                                .addLast(new WebSocketUpgradeHandler("/ws", listener, listenerThreadFactory))
                                .addLast(new WebSocketServerCompressionHandler())
                                .addLast(new WebSocketServerProtocolHandler("/ws", null, true))
                                .addLast(new WebSocketFrameHandler(listenerThreadFactory))
                                .addLast(new WebSocketErrorHandler());
                    }
                });
        return bootstrap;
    }
}
