package org.jaybill.fast.im.user.center.beans;

import com.google.code.kaptcha.util.Config;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class CaptchaConfig {

    @Bean
    public DefaultKaptcha captchaProducer() {
        Properties properties = new Properties();
        // Border
        properties.setProperty("kaptcha.border", "no");
        // Character set, the value of the captcha is obtained from this set.
        properties.setProperty("kaptcha.textproducer.char.string", "ABCDEFGHJKLMNPQRSTWXY23456789");
        // Text color
        properties.setProperty("kaptcha.textproducer.font.color", "0,84,144");
        // Disturbance color
        properties.setProperty("kaptcha.noise.color", "0,84,144");
        // Font size
        properties.setProperty("kaptcha.textproducer.font.size", "30");
        // Background color gradient, start color
        properties.setProperty("kaptcha.background.clear.from", "247,255,234");
        // Background color gradient, end color
        properties.setProperty("kaptcha.background.clear.to", "247,255,234");
        // Image width
        properties.setProperty("kaptcha.image.width", "125");
        // Image height
        properties.setProperty("kaptcha.image.height", "35");
        properties.setProperty("kaptcha.session.key", "code");
        // Char length
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        // Font
        properties.setProperty("kaptcha.textproducer.font.names", "Arial,Courier,cmr10");
        properties.put("kaptcha.textproducer.char.space", "5");
        Config config = new Config(properties);
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }
}
