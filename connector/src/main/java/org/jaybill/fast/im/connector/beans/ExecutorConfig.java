package org.jaybill.fast.im.connector.beans;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ThreadFactory;

@Configuration
public class ExecutorConfig {

    @Bean
    public ThreadFactory listenerThreadFactory() {
        return Thread.ofVirtual()
                .inheritInheritableThreadLocals(false)
                .name("jb_listener_", 0)
                .factory();
    }
}
