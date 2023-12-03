package org.jaybill.fast.im.connector.push;

import io.netty.channel.Channel;
import org.jaybill.fast.im.connector.push.message.Message;

import java.util.Set;

public interface Pusher {

    void channelRegister(String userId, Channel ch);

    void pushIfOnline(Set<String> userIds, Message message);

    void broadcast();
}
