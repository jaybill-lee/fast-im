package org.jaybill.fast.im.connector.controller;

import lombok.extern.slf4j.Slf4j;
import org.jaybill.fast.im.common.util.AssertUtil;
import org.jaybill.fast.im.connector.controller.req.SimplePushReq;
import org.jaybill.fast.im.connector.ws.ChannelEvtHandler;
import org.jaybill.fast.im.connector.ws.evt.PushEvt;
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

    @Post
    public String push(@JsonBody SimplePushReq req) {
        AssertUtil.notNull(req);
        AssertUtil.notNull(req.getUserId());
        AssertUtil.notNull(req.getText());

        evtHandler.pushEvt(PushEvt.builder()
                        .bizId("testBizId")
                        .userId(req.getUserId())
                        .text(req.getText())
                        .includeRemoteChannel(true).build());
        return "{}";
    }
}
