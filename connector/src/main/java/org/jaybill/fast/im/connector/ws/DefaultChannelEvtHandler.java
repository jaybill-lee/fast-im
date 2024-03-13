package org.jaybill.fast.im.connector.ws;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.jaybill.fast.im.common.cache.ChannelManager;
import org.jaybill.fast.im.common.cache.ConnectionKey;
import org.jaybill.fast.im.common.util.IpUtil;
import org.jaybill.fast.im.connector.util.ChannelUtil;
import org.jaybill.fast.im.connector.ws.evt.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DefaultChannelEvtHandler implements ChannelEvtHandler {

    private static final String NEXT_RENEWAL_TIME = "expireTime";
    private static final int DEFAULT_TTL_MINUTE = 30;

    @Autowired
    private LocalChannelManager localChannelManager;
    @Autowired
    private ChannelManager remoteChannelManager;

    @Override
    public void onlineEvt(OnlineEvt evt) {
        localChannelManager.addChannel(evt.getChannel());
        remoteChannelManager.addChannel(
                ConnectionKey.builder()
                        .bizId(evt.getBizId())
                        .userId(evt.getUserId()).build(),
                ChannelUtil.getId(evt.getChannel()),
                IpUtil.getLocalIp(),
                DEFAULT_TTL_MINUTE);
        this.recordNextRenewalTimePoint(evt.getChannel());
    }

    @Override
    public void offlineEvt(OfflineEvt evt) {
        localChannelManager.removeChannel(evt.getChannel());
        remoteChannelManager.removeChannel(
                ConnectionKey.builder()
                        .bizId(evt.getBizId())
                        .userId(evt.getUserId())
                        .build(),
                ChannelUtil.getId(evt.getChannel()));
    }

    @Override
    public void heartbeatEvt(HeartbeatEvt evt) {
        var channel = evt.getChannel();
        long nextTime = (long) channel.attr(AttributeKey.valueOf(NEXT_RENEWAL_TIME)).get();
        if (System.currentTimeMillis() <= nextTime) {
            return;
        }
        remoteChannelManager.renewalChannel(ConnectionKey.builder()
                .bizId(evt.getBizId())
                .userId(evt.getUserId())
                .build(),
                DEFAULT_TTL_MINUTE);
        this.recordNextRenewalTimePoint(channel);

        // TODO: clear out-of-date channel
    }

    @Override
    public void pushEvt(PushEvt evt) {
        // 1.get all local channels and push
        var channels = localChannelManager.getChannels(evt.getBizId(), evt.getUserId());
        channels.forEach(channel -> channel.writeAndFlush(new TextWebSocketFrame(evt.getText()))
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.debug("writeAndFlush success, id:{}", ChannelUtil.getId(channel));
                    } else {
                        log.error("writeAndFlush fail, id:{}, e:", ChannelUtil.getId(channel), future.cause());
                    }
                }));

        // 2.get all remote channels and push
        if (evt.isIncludeRemoteChannel()) {

        }
    }

    @Override
    public void ackEvt(Evt evt) {

    }

    private void recordNextRenewalTimePoint(Channel channel) {
        // Record the most recent timestamp for renewal, and renewal should only occur if it exceeds that timestamp.
        channel.attr(AttributeKey.valueOf(NEXT_RENEWAL_TIME))
                .set(System.currentTimeMillis() + (DEFAULT_TTL_MINUTE - 2) * 60 * 1000);
    }
}
