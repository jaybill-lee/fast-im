package org.jaybill.fast.im.common.web.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
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
            JwtParseUtil.loadCsmsFile(profile);
        }
    }

    public static <T> T getInstance(Class<T> cls) {
        return ctx.getBean(cls);
    }
}
