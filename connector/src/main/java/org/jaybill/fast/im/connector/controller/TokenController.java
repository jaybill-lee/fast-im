package org.jaybill.fast.im.connector.controller;

import lombok.extern.slf4j.Slf4j;
import org.jaybill.fast.im.common.util.AssertUtil;
import org.jaybill.fast.im.connector.controller.filter.AuthFilter;
import org.jaybill.fast.im.connector.controller.req.WsTokenReq;
import org.jaybill.fast.im.connector.util.JwtUtil;
import org.jaybill.fast.im.net.http.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@HttpEndpoint(path = "/token")
public class TokenController {

    /**
     * Get token for websocket client.
     * @return token
     */
    @Post(path = "/ws")
    public String getWsToken(@JsonBody WsTokenReq tokenReq, HttpContext ctx) {
        AssertUtil.notNull(tokenReq);
        AssertUtil.notNull(tokenReq.getTags());
        AssertUtil.notNull(tokenReq.getUserId());

        var bizId = AuthFilter.getBizId(ctx);
        return JwtUtil.createWebsocketToken(bizId, tokenReq.getUserId(), tokenReq.getTags());
    }

}
