package org.jaybill.fast.im.connector.ws;

import org.jaybill.fast.im.connector.ws.evt.Evt;
import org.jaybill.fast.im.connector.ws.evt.HeartbeatEvt;
import org.jaybill.fast.im.connector.ws.evt.OfflineEvt;
import org.jaybill.fast.im.connector.ws.evt.OnlineEvt;

public interface ChannelEvtHandler {
    void onlineEvt(OnlineEvt evt);
    void offlineEvt(OfflineEvt evt);
    void heartbeatEvt(HeartbeatEvt evt);
    void ackEvt(Evt evt);
}
