package org.jaybill.fast.im.connector.netty;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.jaybill.fast.im.common.exception.BaseException;
import org.jaybill.fast.im.connector.util.TcpUtil;

/**
 * Netty websocket global exception handler;
 */
@Slf4j
public class WebSocketErrorHandler extends ChannelHandlerAdapter {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("ws occur err:", cause);
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
