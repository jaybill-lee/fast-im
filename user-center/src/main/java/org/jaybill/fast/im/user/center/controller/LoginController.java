package org.jaybill.fast.im.user.center.controller;

import org.jaybill.fast.im.net.http.HttpEndpoint;
import org.jaybill.fast.im.net.http.Post;
import org.springframework.stereotype.Component;

@Component
@HttpEndpoint(path = "/login")
public class LoginController {

    @Post
    public boolean login() {
        return true;
    }
}
