package org.jaybill.fast.im.connector.util;

import lombok.extern.slf4j.Slf4j;
import org.jaybill.fast.im.common.web.util.JwtParseUtil;
import org.jaybill.fast.im.connector.constant.ChannelBaseConst;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

@Slf4j
public class JwtUtilTest {

    @Before
    public void init() {
        JwtParseUtil.loadCsmsFile("local");
    }

    @Test
    public void testCreateToken_Decode() {
        var bizId = "testBizId";
        var userId = "testUserId";
        var token = JwtUtil.createWebsocketToken(bizId, userId, null);
        log.info("token = {}", token);
        var claims = JwtParseUtil.parseToken(token);
        var actualBizId = claims.get(ChannelBaseConst.BIZ_ID, String.class);
        Assert.assertEquals(bizId, actualBizId);
        var actualUserId = claims.get(ChannelBaseConst.USER_ID, String.class);
        Assert.assertEquals(userId, actualUserId);
        var actualTags = claims.get(ChannelBaseConst.TAGS, List.class);
        Assert.assertNotNull(actualTags);
        Assert.assertEquals(0, actualTags.size());
    }

    @Test
    public void testCreateInternalToken_Decode() {
        var token = JwtUtil.createInternalServiceToken();
        var claims = JwtParseUtil.parseToken(token);
        var iss = claims.getIssuer();
        var aud = claims.getAudience().stream().toList().get(0);
        Assert.assertEquals(JwtUtil.ISSUER, iss);
        Assert.assertEquals(JwtUtil.ISSUER, aud);
    }
}
