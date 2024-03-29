package org.jaybill.fast.im.net;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.jaybill.fast.im.common.exception.BaseException;
import org.jaybill.fast.im.net.util.TcpUtil;

/**
 * Netty websocket global exception handler;
 */
@Slf4j
public class ChannelErrorHandler extends ChannelHandlerAdapter {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("connection occur err:", cause);
        if (cause instanceof NullPointerException) {
            TcpUtil.rst(ctx);
            return;
        }
        if (cause instanceof BaseException) {
            TcpUtil.rst(ctx);
            return;
        }
        TcpUtil.rst(ctx);
    }
}
