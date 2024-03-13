package org.jaybill.fast.im.connector.ws;

import org.jaybill.fast.im.connector.ws.evt.*;

public interface ChannelEvtHandler {
    void onlineEvt(OnlineEvt evt);
    void offlineEvt(OfflineEvt evt);
    void heartbeatEvt(HeartbeatEvt evt);
    void pushEvt(PushEvt evt);
    void ackEvt(Evt evt);
}
