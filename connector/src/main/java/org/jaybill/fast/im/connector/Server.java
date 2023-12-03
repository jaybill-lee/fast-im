package org.jaybill.fast.im.connector;

import io.netty.bootstrap.ServerBootstrap;
import lombok.extern.slf4j.Slf4j;
import org.jaybill.fast.im.connector.beans.BeanName;
import org.jaybill.fast.im.connector.beans.ConnectorProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;


@Slf4j
@SpringBootApplication
public class Server {
    public static void main(String[] args) throws InterruptedException {
        var ctx = SpringApplication.run(Server.class, args);
        var config = ctx.getBean(ConnectorProperties.class);
        var httpServerBootstrap = ctx.getBean(BeanName.HTTP_SERVER_BOOTSTRAP, ServerBootstrap.class);
        var httpPort = config.getHttpPort();
        httpServerBootstrap.bind(httpPort).sync();
        log.info("fast-im-server started, httpPort:{}", httpPort);
        ctx.addApplicationListener((ApplicationListener<ContextClosedEvent>) event -> {
            log.info("fast-im-server stopping");
            httpServerBootstrap.config().group().shutdownGracefully().syncUninterruptibly();
            httpServerBootstrap.config().childGroup().shutdownGracefully().syncUninterruptibly();
            log.info("fast-im-server stopped");
        });
    }
}