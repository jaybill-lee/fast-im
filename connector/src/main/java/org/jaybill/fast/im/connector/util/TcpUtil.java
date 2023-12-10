package org.jaybill.fast.im.connector.util;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;

public class TcpUtil {

    public static void rst(ChannelHandlerContext ctx) {
        if (!ctx.channel().isActive()) {
            return;
        }
        ctx.channel().config().setOption(ChannelOption.SO_LINGER, 0);
        ctx.channel().close();
    }
}
