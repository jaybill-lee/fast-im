package org.jaybill.fast.im.id.server.controller.filter;

import io.jsonwebtoken.JwtException;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.jaybill.fast.im.common.web.util.JwtParseUtil;
import org.jaybill.fast.im.net.http.BaseHttpRequest;
import org.jaybill.fast.im.net.http.HttpContext;
import org.jaybill.fast.im.net.http.HttpFilter;
import org.springframework.stereotype.Component;

@Component
public class AuthFilter implements HttpFilter {

    private static final String APP = "app";

    @Override
    public BeforeResult before(BaseHttpRequest req, HttpContext ctx) {
        try {
            var token = req.header(JwtParseUtil.AUTHORIZATION);
            var claims = JwtParseUtil.parseToken(token);
            var issuer = claims.getIssuer();
            ctx.put(APP, issuer);
        } catch (JwtException e) {
            return HttpFilter.BeforeResult.builder()
                    .continued(false).status(HttpResponseStatus.UNAUTHORIZED).build();
        }
        return HttpFilter.CONTINUED;
    }

    @Override
    public Result after(BaseHttpRequest req, Object returnObj, HttpContext ctx) {
        return null;
    }

    /**
     * get app
     * @return app name
     */
    public static String getApp(HttpContext ctx) {
        return (String) ctx.get(APP);
    }
}
