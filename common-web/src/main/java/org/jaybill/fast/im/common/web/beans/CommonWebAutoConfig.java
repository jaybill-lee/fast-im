package org.jaybill.fast.im.common.web.beans;

import org.jaybill.fast.im.common.web.util.SpringUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonWebAutoConfig {

    @Bean
    public SpringUtil commonSpringUtil() {
        return new SpringUtil();
    }
}
