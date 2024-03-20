package org.jaybill.fast.im.user.center.consts;

import java.text.MessageFormat;

public class RedisKey {
    private static final String CAPTCHA_IMAGE = "u:c:captcha:image:{0}";

    public static String getCaptchaImage(String id) {
        return MessageFormat.format(CAPTCHA_IMAGE, id);
    }
}
