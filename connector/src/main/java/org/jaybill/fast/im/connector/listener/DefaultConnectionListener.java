package org.jaybill.fast.im.connector.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jaybill.fast.im.common.cache.ConnectionCacheHelper;
import org.jaybill.fast.im.common.cache.ConnectionKey;
import org.jaybill.fast.im.common.util.IpUtil;
import org.jaybill.fast.im.connector.client.ConnectorClient;
import org.jaybill.fast.im.connector.listener.evt.Evt;
import org.jaybill.fast.im.connector.listener.evt.OnlineEvt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DefaultConnectionListener implements ConnectionListener {

    @Autowired
    private ConnectionCacheHelper connectionCacheHelper;
    @Autowired
    private ConnectorClient connectorClient;
    @Autowired
    private ConnectionHolder connectionHolder;

    @Override
    public boolean listen(Evt evt) {
        try {
            if (evt instanceof OnlineEvt onlineEvt) {
                return this.handleOnlineEvt(onlineEvt);
            } else {
                // ignore
                return false;
            }
        } catch (Exception e) {
            log.error("handle evt occur err:", e);
            return false;
        }
    }

    private boolean handleOnlineEvt(OnlineEvt onlineEvt) {
        var bizId = onlineEvt.getBizId();
        var userId = onlineEvt.getUserId();
        var platform = onlineEvt.getPlatform().name();
        var connectionKey = ConnectionKey.builder()
                .bizId(bizId)
                .userId(userId)
                .platform(platform)
                .build();

        var serverIp = connectionCacheHelper.getIp(connectionKey);
        // bind the new connection
        connectionCacheHelper.bind(connectionKey, IpUtil.getLocalIp());
        if (StringUtils.isBlank(serverIp)) {
            return true;
        }
        // discard the old connection
        if (IpUtil.getLocalIp().equals(serverIp)) {
            connectionHolder.removeThenHold(bizId, userId, platform, onlineEvt.getChannel());
        } else {
            var future = connectorClient.asyncDiscardConn(serverIp, bizId, userId, platform);
            future.whenComplete((r, t) -> {
                if (t != null) {
                    log.error("async discard conn error, serverId:{}, bizId:{}, userId:{}, e:",
                            serverIp, bizId, userId, t);
                }
            });
        }
        return true;
    }
}
