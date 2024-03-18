package org.jaybill.fast.im.connector.filter;

import org.jaybill.fast.im.connector.constant.ChannelBaseConst;
import org.jaybill.fast.im.connector.util.JwtUtil;
import org.jaybill.fast.im.net.http.BaseHttpRequest;
import org.jaybill.fast.im.net.http.HttpContext;
import org.jaybill.fast.im.net.http.HttpFilter;
import org.springframework.stereotype.Component;

@Component
public class AuthFilter implements HttpFilter  {

    @Override
    public BeforeResult before(BaseHttpRequest req, HttpContext ctx) {
        if (req.path().contains("/local/")) {
            return HttpFilter.BeforeResult.builder().continued(true).build();
        }

        var token = req.header(JwtUtil.AUTHORIZATION);
        var claims = JwtUtil.parseToken(token);
        var bizId = claims.get(ChannelBaseConst.BIZ_ID, String.class);
        ctx.put(ChannelBaseConst.BIZ_ID, bizId);
        return HttpFilter.BeforeResult.builder().continued(true).build();
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
