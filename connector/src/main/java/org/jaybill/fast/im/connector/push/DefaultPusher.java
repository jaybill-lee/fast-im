package org.jaybill.fast.im.connector.push;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.jaybill.fast.im.connector.push.message.Message;
import org.jaybill.fast.im.common.util.IpUtil;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class DefaultPusher implements Pusher {

    private final Map<String, Channel> channelMap = new ConcurrentHashMap<>();

    @Override
    public void channelRegister(String userId, Channel ch) {
        this.channelMap.put(userId, ch);
    }

    @Override
    public void pushIfOnline(Set<String> userIds, Message message) {
        if (CollectionUtils.isEmpty(userIds) || message == null) {
            return;
        }

        var userIpMap = this.getUserConnectedIp(userIds);
        if (MapUtils.isEmpty(userIpMap)) {
            return;
        }
        userIpMap.forEach((userId, ip) -> {
            if (IpUtil.getLocalIp().equals(ip)) {
                // native call
                var ch = channelMap.get(userId);
                if (ch != null) {
                    ch.writeAndFlush(message);
                }
            } else {
                // http call

            }
        });
    }

    @Override
    public void broadcast() {

    }

    private Map<String, String> getUserConnectedIp(Set<String> userIds) {
        return null;
    }
}
