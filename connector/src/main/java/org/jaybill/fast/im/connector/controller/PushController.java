package org.jaybill.fast.im.connector.controller;

import lombok.extern.slf4j.Slf4j;
import org.jaybill.fast.im.connector.controller.filter.AuthFilter;
import org.jaybill.fast.im.connector.ws.ChannelEvtHandler;
import org.jaybill.fast.im.connector.ws.PushResult;
import org.jaybill.fast.im.connector.ws.evt.PushEvt;
import org.jaybill.fast.im.net.http.HttpContext;
import org.jaybill.fast.im.net.http.HttpEndpoint;
import org.jaybill.fast.im.net.http.JsonBody;
import org.jaybill.fast.im.net.http.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@HttpEndpoint(path = "/push")
public class PushController {

    @Autowired
    private ChannelEvtHandler evtHandler;

    /**
     * Push to specify user. One user maybe has more than one channel.
     */
    @Post
    public PushResult push(@JsonBody PushEvt evt, HttpContext ctx) {
        var bizId = AuthFilter.getBizId(ctx);
        evt.setBizId(bizId); // force reset
        return evtHandler.pushEvt(evt);
    }
}
