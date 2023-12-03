package org.jaybill.fast.im.connector.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.jaybill.fast.im.common.util.IdUtil;
import org.jaybill.fast.im.common.util.JsonUtil;
import org.jaybill.fast.im.connector.constant.BaseConst;
import org.jaybill.fast.im.connector.constant.enums.PlatformEnum;
import org.jaybill.fast.im.connector.listener.ConnectionListener;
import org.jaybill.fast.im.connector.listener.evt.OnlineEvt;
import org.jaybill.fast.im.connector.util.JwtUtil;
import org.jaybill.fast.im.connector.util.TcpUtil;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadFactory;

@Slf4j
public class WebSocketUpgradeHandler extends SimpleChannelInboundHandler<FullHttpRequest>  {

    private final String wsPath;
    private final ConnectionListener listener;
    private final ThreadFactory threadFactory;

    public WebSocketUpgradeHandler(String wsPath, ConnectionListener listener, ThreadFactory threadFactory) {
        this.wsPath = wsPath;
        this.listener = listener;
        this.threadFactory = threadFactory;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) {
        if (req.headers().contains(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET, true)) {
            var token = req.headers().get(JwtUtil.AUTHORIZATION);
            try {
                var channel = ctx.channel();
                var claims = JwtUtil.parseToken(token);

                var bizId = claims.get(BaseConst.BIZ_ID, String.class);
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

                // Notify listener to handle online evt.
                var future = new CompletableFuture<Boolean>();
                threadFactory.newThread(() -> {
                    var success = listener.listen(OnlineEvt.builder()
                            .id(IdUtil.getUuid())
                            .bizId(bizId)
                            .userId(userId)
                            .platform(platform)
                            .channel(channel)
                            .build());
                    future.complete(success);
                }).start();

                // We need to retain the msg, because the thenAccept() will be async exec,
                // then the super-class will release the msg if we not retain the msg.
                req.retain();
                future.thenAccept((r) -> {
                   if (r) {
                       ctx.fireChannelRead(req);
                   } else {
                       log.error("online evt handle fail, bizId:{}, userId:{}, tags:{}",
                               bizId, userId, JsonUtil.toJson(tags));
                       TcpUtil.rst(ctx);
                   }
                }).whenComplete((r, e) -> {
                    if (e != null) {
                        log.error("future then accept error:", e);
                    }
                });
            } catch (Throwable e) {
                log.error("parse token error, rst connection:", e);
                TcpUtil.rst(ctx);
            }
        }
    }
}
