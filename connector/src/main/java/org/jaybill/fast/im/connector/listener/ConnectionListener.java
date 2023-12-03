package org.jaybill.fast.im.connector.listener;

import org.jaybill.fast.im.connector.listener.evt.Evt;

public interface ConnectionListener {
    boolean listen(Evt evt);
}
