package org.jaybill.fast.im.connector.controller.internal;

import org.jaybill.fast.im.connector.ws.DefaultChannelEvtHandler;
import org.jaybill.fast.im.connector.ws.PushResult;
import org.jaybill.fast.im.connector.ws.evt.InternalPushEvt;
import org.jaybill.fast.im.net.http.HttpEndpoint;
import org.jaybill.fast.im.net.http.JsonBody;
import org.jaybill.fast.im.net.http.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Only for internal call.
 */
@Component
@HttpEndpoint(path = "/local/push")
public class LocalPushController {

    @Autowired
    private DefaultChannelEvtHandler evtHandler;

    @Post
    public PushResult push(@JsonBody InternalPushEvt evt) {
        return evtHandler.localPushEvt(evt);
    }
}
