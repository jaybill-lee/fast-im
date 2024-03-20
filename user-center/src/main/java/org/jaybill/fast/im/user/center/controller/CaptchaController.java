package org.jaybill.fast.im.user.center.controller;

import org.jaybill.fast.im.common.util.IdUtil;
import org.jaybill.fast.im.net.http.Get;
import org.jaybill.fast.im.net.http.HttpEndpoint;
import org.jaybill.fast.im.user.center.dto.CaptchaDTO;
import org.jaybill.fast.im.user.center.service.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@HttpEndpoint(path = "/captcha")
public class CaptchaController {

    @Autowired
    private CaptchaService captchaService;

    /**
     * create captcha
     */
    @Get
    public CaptchaDTO create() {
        var id = IdUtil.getUuid();
        var captchaBase64 = captchaService.createCaptchaBase64(id);
        return new CaptchaDTO(id, captchaBase64);
    }
}
