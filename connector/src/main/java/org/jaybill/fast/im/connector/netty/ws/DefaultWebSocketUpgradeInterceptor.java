package org.jaybill.fast.im.connector.netty.ws;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.jaybill.fast.im.common.util.IdUtil;
import org.jaybill.fast.im.connector.constant.BaseConst;
import org.jaybill.fast.im.connector.constant.enums.PlatformEnum;
import org.jaybill.fast.im.connector.ex.BeforeUpgradeException;
import org.jaybill.fast.im.connector.ex.OnlineEvtHandleException;
import org.jaybill.fast.im.connector.listener.ConnectionListener;
import org.jaybill.fast.im.connector.listener.evt.OnlineEvt;
import org.jaybill.fast.im.connector.util.JwtUtil;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadFactory;

@Slf4j
public class DefaultWebSocketUpgradeInterceptor implements WebSocketUpgradeInterceptor {

    private final ConnectionListener listener;
    private final ThreadFactory threadFactory;

    public DefaultWebSocketUpgradeInterceptor(ConnectionListener listener, ThreadFactory threadFactory) {
        this.listener = listener;
        this.threadFactory = threadFactory;
    }

    @Override
    public CompletableFuture<?> before(ChannelHandlerContext ctx, FullHttpRequest req) {
        var future = new CompletableFuture<Boolean>();
        var token = req.headers().get(JwtUtil.AUTHORIZATION);
        try {
            var channel = ctx.channel();
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

            // Notify listener to handle online evt.
            threadFactory.newThread(() -> {
                var success = listener.listen(OnlineEvt.builder()
                        .id(IdUtil.getUuid())
                        .bizId(bizId)
                        .userId(userId)
                        .platform(platform)
                        .channel(channel)
                        .build());
                if (success) {
                    future.complete(true);
                } else {
                    future.completeExceptionally(new OnlineEvtHandleException());
                }
            }).start();

        } catch (Throwable e) {
            log.error("channel before upgrade evt handle error:", e);
            future.completeExceptionally(new BeforeUpgradeException());
        }
        return future;
    }
}