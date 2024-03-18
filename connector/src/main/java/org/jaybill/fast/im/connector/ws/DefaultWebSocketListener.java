package org.jaybill.fast.im.connector.ws;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.jaybill.fast.im.common.util.IdUtil;
import org.jaybill.fast.im.connector.constant.ChannelBaseConst;
import org.jaybill.fast.im.connector.constant.enums.PlatformEnum;
import org.jaybill.fast.im.connector.util.JwtUtil;
import org.jaybill.fast.im.connector.ws.evt.HeartbeatEvt;
import org.jaybill.fast.im.connector.ws.evt.OfflineEvt;
import org.jaybill.fast.im.connector.ws.evt.OnlineEvt;
import org.jaybill.fast.im.net.ws.WebSocketListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class DefaultWebSocketListener implements WebSocketListener {

    @Autowired
    private ChannelEvtHandler evtHandler;

    @Override
    public boolean beforeHandshake(ChannelHandlerContext ctx, HttpMessage req) {
        var token = req.headers().get(JwtUtil.AUTHORIZATION);
        try {
            var channel = ctx.channel();
            channel.attr(AttributeKey.valueOf(ChannelBaseConst.CHANNEL_ID)).setIfAbsent(IdUtil.getUuid());

            var claims = JwtUtil.parseToken(token);
            var bizId = claims.get(ChannelBaseConst.BIZ_ID, String.class);
            var bizIdAttr = channel.attr(AttributeKey.valueOf(ChannelBaseConst.BIZ_ID));
            bizIdAttr.set(bizId);

            var userId = claims.get(ChannelBaseConst.USER_ID, String.class);
            var userIdAttr = channel.attr(AttributeKey.valueOf(ChannelBaseConst.USER_ID));
            userIdAttr.set(userId);

            var platform0 = claims.get(ChannelBaseConst.PLATFORM, String.class);
            var platform = PlatformEnum.fromName(platform0);
            var platformAttr = channel.attr(AttributeKey.valueOf(ChannelBaseConst.PLATFORM));
            platformAttr.set(platform);

            var tags = claims.get(ChannelBaseConst.TAGS, List.class);
            var tagsAttr = channel.attr(AttributeKey.valueOf(ChannelBaseConst.TAGS));
            tagsAttr.set(tags);

            return true;
        } catch (Throwable e) {
            log.error("channel before upgrade evt handle error:", e);
            return false;
        }
    }

    @Override
    public void onHandshakeSuccess(ChannelHandlerContext ctx) {
        var channel = ctx.channel();
        var bizId = (String) channel.attr(AttributeKey.valueOf(ChannelBaseConst.BIZ_ID)).get();
        var userId = (String) channel.attr(AttributeKey.valueOf(ChannelBaseConst.USER_ID)).get();
        var platform = (PlatformEnum) channel.attr(AttributeKey.valueOf(ChannelBaseConst.PLATFORM)).get();

        // Notify listener to handle online evt.
        if (!channel.isActive()) {
            log.debug("channel is close");
            return;
        }
        var onlineAttr = channel.attr(AttributeKey.valueOf(ChannelBaseConst.ONLINE));
        onlineAttr.set(true);
        evtHandler.onlineEvt(OnlineEvt.builder()
                .id(IdUtil.getUuid())
                .bizId(bizId)
                .userId(userId)
                .platform(platform)
                .channel(channel)
                .build());
    }

    @Override
    public void onHandshakeFail(ChannelHandlerContext ctx) {
        log.error("ws upgrade fail");
    }

    @Override
    public void onChannelHeartbeat(ChannelHandlerContext ctx) {
        log.debug("onChannelHeartbeat, id:{}", ctx.channel().id());
        var channel = ctx.channel();
        var bizId = (String) channel.attr(AttributeKey.valueOf(ChannelBaseConst.BIZ_ID)).get();
        var userId = (String) channel.attr(AttributeKey.valueOf(ChannelBaseConst.USER_ID)).get();
        var platform = (PlatformEnum) channel.attr(AttributeKey.valueOf(ChannelBaseConst.PLATFORM)).get();
        evtHandler.heartbeatEvt(HeartbeatEvt.builder()
                .id(IdUtil.getUuid())
                .bizId(bizId)
                .userId(userId)
                .platform(platform)
                .channel(channel)
                .build());
    }

    @Override
    public void onChannelTextFrame(ChannelHandlerContext ctx, String text) {
        log.debug("onChannelTextFrame:{}", text);
    }

    @Override
    public void onChannelClose(ChannelHandlerContext ctx) {
        var channel = ctx.channel();
        if (!channel.hasAttr(AttributeKey.valueOf(ChannelBaseConst.ONLINE))) {
            log.debug("online evt has not trigger, do not need to clear resource");
            return;
        }
        var bizId = (String) channel.attr(AttributeKey.valueOf(ChannelBaseConst.BIZ_ID)).get();
        var userId = (String) channel.attr(AttributeKey.valueOf(ChannelBaseConst.USER_ID)).get();
        var platform = (PlatformEnum) channel.attr(AttributeKey.valueOf(ChannelBaseConst.PLATFORM)).get();
        evtHandler.offlineEvt(OfflineEvt.builder()
                .id(IdUtil.getUuid())
                .bizId(bizId)
                .userId(userId)
                .platform(platform)
                .channel(channel)
                .build());
    }
}
