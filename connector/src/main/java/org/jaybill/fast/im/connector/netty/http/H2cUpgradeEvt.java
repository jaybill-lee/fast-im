package org.jaybill.fast.im.connector.netty.http;

import io.netty.handler.codec.http.FullHttpRequest;

public record H2cUpgradeEvt(CharSequence protocol, FullHttpRequest upgradeRequest) {

    /**
     * The protocol that the channel has been upgraded to.
     */
    @Override
    public CharSequence protocol() {
        return protocol;
    }

    /**
     * Gets the request that triggered the protocol upgrade.
     */
    @Override
    public FullHttpRequest upgradeRequest() {
        return upgradeRequest;
    }

    @Override
    public String toString() {
        return "UpgradeEvent [protocol=" + protocol + ", upgradeRequest=" + upgradeRequest + ']';
    }
}
