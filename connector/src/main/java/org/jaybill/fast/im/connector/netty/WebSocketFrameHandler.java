package org.jaybill.fast.im.connector.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.jaybill.fast.im.connector.constant.BaseConst;
import org.jaybill.fast.im.connector.netty.dto.WsDataDTO;
import org.jaybill.fast.im.connector.service.UserService;
import org.jaybill.fast.im.common.util.JsonUtil;

import java.util.Optional;
import java.util.concurrent.ThreadFactory;

@Slf4j
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private ThreadFactory threadFactory;

    public WebSocketFrameHandler(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) {
        if (!(msg instanceof TextWebSocketFrame textMsg)) {
           return;
        }
        var textFrame = textMsg.text();
    }
}
