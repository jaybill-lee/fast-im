package org.jaybill.fast.im.connector.controller;

import org.jaybill.fast.im.net.http.HttpEndpoint;
import org.jaybill.fast.im.net.http.Post;

@HttpEndpoint(path = "/push")
public class PushController {

    @Post
    public String push() {
        return null;
    }
}
