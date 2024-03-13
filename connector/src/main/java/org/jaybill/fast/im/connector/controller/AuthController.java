package org.jaybill.fast.im.connector.controller;

import lombok.extern.slf4j.Slf4j;
import org.jaybill.fast.im.net.http.Get;
import org.jaybill.fast.im.net.http.HttpEndpoint;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@HttpEndpoint(path = "/auth")
public class AuthController {

    @Get
    public String auth(String bizId) {
        return bizId;
    }

}
