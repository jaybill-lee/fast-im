package org.jaybill.fast.im.connector.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SpringUtil implements ApplicationContextAware {

    private static ApplicationContext ctx;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext;

        log.debug("onApplicationEvent, load csms file");
        var profiles = ctx.getEnvironment().getActiveProfiles();
        if (profiles.length > 1) {
            log.warn("active profile > 1, key maybe cover");
        }

        for (String profile : profiles) {
            JwtUtil.loadCsmsFile(profile);
        }
    }

    public static <T> T getInstance(Class<T> cls) {
        return ctx.getBean(cls);
    }
}
