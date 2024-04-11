package org.jaybill.fast.im.user.center.util;

import lombok.extern.slf4j.Slf4j;
import org.jaybill.fast.im.common.web.util.JwtParseUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@Slf4j
public class JwtUtilTest {

    @Before
    public void init() {
        JwtParseUtil.loadCsmsFile("local");
    }

    @Test
    public void createToken() {
        var token = JwtUtil.createServiceToken();
        log.debug("token = {}", token);
        var claims = JwtParseUtil.parseToken(token);
        Assert.assertEquals(claims.getIssuer(), JwtUtil.ISSUER);
        Assert.assertEquals(claims.getAudience().stream().toList().get(0), JwtUtil.AUD);
    }
}
