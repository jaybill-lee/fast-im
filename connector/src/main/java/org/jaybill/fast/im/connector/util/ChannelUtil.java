package org.jaybill.fast.im.connector.util;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.jaybill.fast.im.connector.constant.ChannelBaseConst;

public class ChannelUtil {

    public static String getId(Channel channel) {
        return channel.attr(AttributeKey.valueOf(ChannelBaseConst.CHANNEL_ID)).get().toString();
    }
}
