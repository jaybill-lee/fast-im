package org.jaybill.fast.im.connector.ws;

import org.jaybill.fast.im.connector.ws.evt.*;

public interface ChannelEvtHandler {
    void onlineEvt(OnlineEvt evt);
    void offlineEvt(OfflineEvt evt);
    void heartbeatEvt(HeartbeatEvt evt);
    PushResult pushEvt(PushEvt evt);
    PushResult localPushEvt(InternalPushEvt evt);
    void ackEvt(AckEvt evt);
}
