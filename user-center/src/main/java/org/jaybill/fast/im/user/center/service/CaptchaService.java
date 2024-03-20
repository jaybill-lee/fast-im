package org.jaybill.fast.im.user.center.service;

public interface CaptchaService {

    /**
     * Create captcha image in base64 format
     * @param id unique id
     * @return image base64
     */
    String createCaptchaBase64(String id);
}
