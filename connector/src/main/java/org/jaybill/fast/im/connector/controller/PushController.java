package org.jaybill.fast.im.connector.controller;

import org.jaybill.fast.im.connector.netty.http.HttpEndpoint;
import org.jaybill.fast.im.connector.netty.http.Post;

@HttpEndpoint(path = "/push")
public class PushController {

    @Post
    public String push() {
        return null;
    }
}
