package org.jaybill.fast.im.connector.ws;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.jaybill.fast.im.common.util.IdUtil;
import org.jaybill.fast.im.connector.constant.BaseConst;
import org.jaybill.fast.im.connector.constant.enums.PlatformEnum;
import org.jaybill.fast.im.connector.util.JwtUtil;
import org.jaybill.fast.im.connector.ws.evt.HeartbeatEvt;
import org.jaybill.fast.im.connector.ws.evt.OfflineEvt;
import org.jaybill.fast.im.connector.ws.evt.OnlineEvt;
import org.jaybill.fast.im.net.ws.WebSocketListener;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class DefaultWebSocketListener implements WebSocketListener {

    private final ChannelEvtHandler evtHandler;

    public DefaultWebSocketListener(ChannelEvtHandler evtHandler) {
        this.evtHandler = evtHandler;
    }

    @Override
    public boolean beforeHandshake(ChannelHandlerContext ctx, HttpMessage req) {
        var token = req.headers().get(JwtUtil.AUTHORIZATION);
        try {
            var channel = ctx.channel();
            channel.attr(AttributeKey.valueOf(BaseConst.CHANNEL_ID)).setIfAbsent(IdUtil.getUuid());

            var claims = JwtUtil.parseToken(token);
            var bizId = claims.getAudience().stream().findFirst().orElse("");
            var bizIdAttr = channel.attr(AttributeKey.valueOf(BaseConst.BIZ_ID));
            bizIdAttr.set(bizId);

            var userId = claims.get(BaseConst.USER_ID, String.class);
            var userIdAttr = channel.attr(AttributeKey.valueOf(BaseConst.USER_ID));
            userIdAttr.set(userId);

            var platform0 = claims.get(BaseConst.PLATFORM, String.class);
            var platform = PlatformEnum.fromName(platform0);
            var platformAttr = channel.attr(AttributeKey.valueOf(BaseConst.PLATFORM));
            platformAttr.set(platform);

            var tags = claims.get(BaseConst.TAGS, List.class);
            var tagsAttr = channel.attr(AttributeKey.valueOf(BaseConst.TAGS));
            tagsAttr.set(tags);

            var lock = new ReentrantLock();
            var lockAttr = channel.attr(AttributeKey.valueOf(BaseConst.LOCK));
            lockAttr.set(lock);
            return true;
        } catch (Throwable e) {
            log.error("channel before upgrade evt handle error:", e);
            return false;
        }
    }

    @Override
    public void onHandshakeSuccess(ChannelHandlerContext ctx) {
        var channel = ctx.channel();
        var bizId = (String) channel.attr(AttributeKey.valueOf(BaseConst.BIZ_ID)).get();
        var userId = (String) channel.attr(AttributeKey.valueOf(BaseConst.USER_ID)).get();
        var platform = (PlatformEnum) channel.attr(AttributeKey.valueOf(BaseConst.PLATFORM)).get();

        // Notify listener to handle online evt.
        var lock = (ReentrantLock) channel.attr(AttributeKey.valueOf(BaseConst.LOCK)).get();
        try {
            lock.lock();
            if (!channel.isActive()) {
                log.debug("channel is close");
                return;
            }
            var onlineAttr = channel.attr(AttributeKey.valueOf(BaseConst.ONLINE));
            onlineAttr.set(true);
            evtHandler.onlineEvt(OnlineEvt.builder()
                    .id(IdUtil.getUuid())
                    .bizId(bizId)
                    .userId(userId)
                    .platform(platform)
                    .channel(channel)
                    .build());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void onHandshakeFail(ChannelHandlerContext ctx) {
        log.error("ws upgrade fail");
    }

    @Override
    public void onChannelHeartbeat(ChannelHandlerContext ctx) {
        log.debug("onChannelHeartbeat, id:{}", ctx.channel().id());
        var channel = ctx.channel();
        var bizId = (String) channel.attr(AttributeKey.valueOf(BaseConst.BIZ_ID)).get();
        var userId = (String) channel.attr(AttributeKey.valueOf(BaseConst.USER_ID)).get();
        var platform = (PlatformEnum) channel.attr(AttributeKey.valueOf(BaseConst.PLATFORM)).get();
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

        var lock = (ReentrantLock) channel.attr(AttributeKey.valueOf(BaseConst.LOCK)).get();
        try {
            lock.lock();
            if (!channel.hasAttr(AttributeKey.valueOf(BaseConst.ONLINE))) {
                log.debug("online evt has not trigger, do not need to clear resource");
                return;
            }
            var bizId = (String) channel.attr(AttributeKey.valueOf(BaseConst.BIZ_ID)).get();
            var userId = (String) channel.attr(AttributeKey.valueOf(BaseConst.USER_ID)).get();
            var platform = (PlatformEnum) channel.attr(AttributeKey.valueOf(BaseConst.PLATFORM)).get();
            evtHandler.offlineEvt(OfflineEvt.builder()
                    .id(IdUtil.getUuid())
                    .bizId(bizId)
                    .userId(userId)
                    .platform(platform)
                    .channel(channel)
                    .build());
        } finally {
            lock.unlock();
        }
    }
}
