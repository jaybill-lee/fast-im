package org.jaybill.fast.im.connector.ws.strategy;

import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.jaybill.fast.im.common.cache.ChannelManager;
import org.jaybill.fast.im.common.util.IpUtil;
import org.jaybill.fast.im.common.util.JsonUtil;
import org.jaybill.fast.im.connector.client.ConnectorClient;
import org.jaybill.fast.im.connector.util.ChannelUtil;
import org.jaybill.fast.im.connector.util.FutureUtil;
import org.jaybill.fast.im.connector.ws.LocalChannelManager;
import org.jaybill.fast.im.connector.ws.PushAck;
import org.jaybill.fast.im.connector.ws.PushResult;
import org.jaybill.fast.im.connector.ws.evt.AckEvt;
import org.jaybill.fast.im.connector.ws.evt.InternalPushEvt;
import org.jaybill.fast.im.net.spring.properties.ConnectorProperties;
import org.jaybill.fast.im.net.util.UriUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class AckPushStrategy implements PushStrategy {

    @Autowired
    private LocalChannelManager localChannelManager;
    @Autowired
    private ChannelManager remoteChannelManager;
    @Autowired
    private ConnectorClient connectorClient;
    @Autowired
    private ConnectorProperties connectorProperties;
    @Value("${ws.strategy.ack.default-timeout:30}")
    private int defaultTimeoutSec;

    private final Map<String, CompletableFuture<PushAck>> pendingMessageFutureMap = new ConcurrentHashMap<>();

    @Override
    public PushResult push(InternalPushEvt evt) {
        this.normalise(evt);
        var message = evt.getMessage();
        var id = message.getId();

        // 1.get all local channels and push
        var channels = localChannelManager.getChannels(evt.getBizId(), evt.getUserId());
        var futureList = new ArrayList<CompletableFuture<PushAck>>();
        for (var channel : channels) {
            var future = new CompletableFuture<PushAck>();
            futureList.add(future);
            pendingMessageFutureMap.put(id, future);
            channel.writeAndFlush(
                new TextWebSocketFrame(JsonUtil.toJson(message)))
                    .addListener((ChannelFutureListener) f -> {
                        if (f.isSuccess()) {
                            log.debug("write and flush message success, id:{}", id);
                        } else {
                            var error = f.cause();
                            future.completeExceptionally(error);
                            log.error("write and flush message error, id:{}, e:", id, error);
                        }
                    });
        }

        // 2.get all remote channels and push
        var httpFutureList = new ArrayList<CompletableFuture<PushResult>>();
        if (evt.isEnableRemotePush()) {
            var channelId2ServerAddressMap = remoteChannelManager.getServerAddress(evt.getBizId(), evt.getUserId());
            var serverAddressSet = new HashSet<>(channelId2ServerAddressMap.values());
            serverAddressSet.forEach(serverAddress -> {
                if (UriUtil.buildServerAddress(IpUtil.getLocalIp(),
                        connectorProperties.getHttpPort()).equals(serverAddress)) {
                    return;
                }
                var httpFuture = connectorClient.localPush(serverAddress, InternalPushEvt.builder()
                        .bizId(evt.getBizId())
                        .userId(evt.getUserId())
                        .message(message)
                        .withAck(true)
                        .timeout(evt.getTimeout())
                        .unit(evt.getUnit())
                        .build());
                httpFutureList.add(httpFuture);
            });
        }

        var ackChannelList = new ArrayList<String>();
        // 3. wait for result
        var remainingTimeoutMillis = FutureUtil.waiting(futureList, evt.getTimeout(), evt.getUnit(), (ack) -> ackChannelList.add(ack.getChannelId()));
        FutureUtil.waiting(httpFutureList, remainingTimeoutMillis, TimeUnit.MILLISECONDS, (res) -> ackChannelList.addAll(res.getAckChannelIds()));

        log.debug("all ack channels:{}", ackChannelList);
        return new PushResult(id, ackChannelList);
    }

    public void ack(AckEvt evt) {
        var id = evt.getId();
        var future = pendingMessageFutureMap.get(id);
        if (future != null) {
            // success
            future.complete(new PushAck(ChannelUtil.getId(evt.getChannel()), id));
        }
    }

    private void normalise(InternalPushEvt evt) {
        if (evt.getTimeout() <= 0 || evt.getUnit() == null) {
            evt.setTimeout(defaultTimeoutSec);
            evt.setUnit(TimeUnit.SECONDS);
        }
    }
}
