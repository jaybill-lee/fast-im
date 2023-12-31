package org.jaybill.fast.im.connector.controller;

import lombok.extern.slf4j.Slf4j;
import org.jaybill.fast.im.connector.netty.http.Get;
import org.jaybill.fast.im.connector.netty.http.HttpEndpoint;

@Slf4j
@HttpEndpoint(path = "/auth")
public class AuthController {

    @Get
    public String auth(String bizId) {
        return bizId;
    }

}
