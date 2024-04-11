package org.jaybill.fast.im.connector.controller.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.jaybill.fast.im.common.web.util.JwtParseUtil;
import org.jaybill.fast.im.connector.constant.ChannelBaseConst;
import org.jaybill.fast.im.connector.util.JwtUtil;
import org.jaybill.fast.im.net.http.BaseHttpRequest;
import org.jaybill.fast.im.net.http.HttpContext;
import org.jaybill.fast.im.net.http.HttpFilter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthFilter implements HttpFilter  {

    @Override
    public BeforeResult before(BaseHttpRequest req, HttpContext ctx) {
        Claims claims;
        var token = req.header(JwtParseUtil.AUTHORIZATION);
        try {
            claims = JwtParseUtil.parseToken(token);
        } catch (JwtException e) {
            log.error("jwt parse error, uri:{}, e:", req.path(), e);
            return HttpFilter.BeforeResult.builder()
                    .continued(false).status(HttpResponseStatus.UNAUTHORIZED).build();
        }

        // 1. internal url
        if (req.path().contains("/local/")) {
            var issuer = claims.getIssuer();
            var aud = claims.getAudience().stream().toList().get(0);
            // check if issue and aud all is myself
            if (issuer.equals(JwtUtil.ISSUER) && aud.equals(JwtUtil.ISSUER)) {
                return HttpFilter.CONTINUED;
            }
            log.error("jwt issuer and aud verify error, uri:{}, issuer:{}, aud:{}", req.path(), issuer, aud);
            return HttpFilter.BeforeResult.builder()
                    .continued(false).status(HttpResponseStatus.UNAUTHORIZED).build();
        }

        // 2. external url
        var bizId = claims.get(ChannelBaseConst.BIZ_ID, String.class);
        ctx.put(ChannelBaseConst.BIZ_ID, bizId);
        return HttpFilter.CONTINUED;
    }

    @Override
    public Result after(BaseHttpRequest req, Object returnObj, HttpContext ctx) {
        return null;
    }

    public static String getBizId(HttpContext ctx) {
        var v = ctx.get(ChannelBaseConst.BIZ_ID);
        if (v == null) {
            return null;
        }
        return v.toString();
    }
}
