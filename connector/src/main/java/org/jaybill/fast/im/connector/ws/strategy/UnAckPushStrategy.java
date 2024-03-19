package org.jaybill.fast.im.connector.ws.strategy;

import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.jaybill.fast.im.common.cache.ChannelManager;
import org.jaybill.fast.im.common.util.IpUtil;
import org.jaybill.fast.im.common.util.JsonUtil;
import org.jaybill.fast.im.connector.client.ConnectorClient;
import org.jaybill.fast.im.connector.ws.LocalChannelManager;
import org.jaybill.fast.im.connector.ws.PushResult;
import org.jaybill.fast.im.connector.ws.evt.InternalPushEvt;
import org.jaybill.fast.im.net.spring.properties.ConnectorProperties;
import org.jaybill.fast.im.net.util.UriUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;

@Slf4j
@Component
public class UnAckPushStrategy implements PushStrategy {

    @Autowired
    private LocalChannelManager localChannelManager;
    @Autowired
    private ChannelManager remoteChannelManager;
    @Autowired
    private ConnectorClient connectorClient;
    @Autowired
    private ConnectorProperties connectorProperties;

    @Override
    public PushResult push(InternalPushEvt evt) {
        var message = evt.getMessage();
        var id = message.getId();

        // 1.get all local channels and push
        var channels = localChannelManager.getChannels(evt.getBizId(), evt.getUserId());
        for (var channel : channels) {
            channel.writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(message)))
                .addListener((ChannelFutureListener) f -> {
                    if (f.isSuccess()) {
                        log.debug("write and flush message success, id:{}", id);
                    } else {
                        var error = f.cause();
                        log.error("write and flush message error, id:{}, e:", id, error);
                    }
                });
        }

        // 2.get all remote channels and push
        if (evt.isEnableRemotePush()) {
            var channelId2ServerAddressMap = remoteChannelManager.getServerAddress(evt.getBizId(), evt.getUserId());
            var serverAddressSet = new HashSet<>(channelId2ServerAddressMap.values());
            serverAddressSet.forEach(serverAddress -> {
                if (UriUtil.buildServerAddress(IpUtil.getLocalIp(),
                        connectorProperties.getHttpPort()).equals(serverAddress)) {
                    return;
                }
                connectorClient.localPush(serverAddress, InternalPushEvt.builder()
                    .bizId(evt.getBizId())
                    .userId(evt.getUserId())
                    .message(message)
                    .withAck(false)
                    .timeout(evt.getTimeout())
                    .unit(evt.getUnit())
                    .build());
            });
        }

        return new PushResult(id, Collections.emptyList());
    }
}
