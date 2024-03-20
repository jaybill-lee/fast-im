package org.jaybill.fast.im.user.center.controller;

import org.jaybill.fast.im.net.http.HttpEndpoint;
import org.jaybill.fast.im.net.http.JsonBody;
import org.jaybill.fast.im.net.http.Post;
import org.jaybill.fast.im.user.center.dto.UserSimpleDTO;
import org.jaybill.fast.im.user.center.dto.req.RegisterReq;
import org.jaybill.fast.im.user.center.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@HttpEndpoint(path = "/register")
public class RegisterController {

    @Autowired
    private RegisterService registerService;

    @Post
    public UserSimpleDTO register(@JsonBody RegisterReq registerReq) {
        return registerService.register(registerReq);
    }
}
