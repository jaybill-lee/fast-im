package org.jaybill.fast.im.user.center.service.impl;

import org.jaybill.fast.im.common.util.AssertUtil;
import org.jaybill.fast.im.user.center.dto.UserSimpleDTO;
import org.jaybill.fast.im.user.center.dto.req.RegisterReq;
import org.jaybill.fast.im.user.center.service.CaptchaService;
import org.jaybill.fast.im.user.center.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegisterServiceImpl implements RegisterService {

    private static final String PASSWD_PATTERN = "^[a-zA-Z0-9]+$";

    @Autowired
    private CaptchaService captchaService;

    @Override
    public UserSimpleDTO register(RegisterReq req) {
        // base check
        AssertUtil.notNull(req);
        AssertUtil.notNull(req.getCaptchaId());
        AssertUtil.notNull(req.getCaptchaText());
        AssertUtil.notNull(req.getNickname());
        AssertUtil.notNull(req.getPassword());
        AssertUtil.notNull(req.getConfirmPassword());
        AssertUtil.isTrue(req.getPassword().length() >= 6);
        AssertUtil.isTrue(req.getPassword().length() <= 16);
        AssertUtil.isTrue(req.getPassword().matches(PASSWD_PATTERN));
        AssertUtil.isTrue(req.getPassword().equals(req.getConfirmPassword()));

        // check captcha
        boolean match = captchaService.validateText(req.getCaptchaId(), req.getCaptchaText());
        AssertUtil.isTrue(match);

        // assign id number

        // insert db

        return null;
    }
}
