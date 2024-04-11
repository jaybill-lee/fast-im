package org.jaybill.fast.im.connector.ws.strategy;

import org.jaybill.fast.im.common.web.util.SpringUtil;
import org.jaybill.fast.im.connector.ws.PushResult;
import org.jaybill.fast.im.connector.ws.evt.InternalPushEvt;

public interface PushStrategy {

    PushResult push(InternalPushEvt evt);

    static PushStrategy getInstance(boolean withAck) {
        if (withAck) {
            return SpringUtil.getInstance(AckPushStrategy.class);
        }
        return SpringUtil.getInstance(UnAckPushStrategy.class);
    }
}
