package org.jaybill.fast.im.connector.util;

import lombok.extern.slf4j.Slf4j;
import org.jaybill.fast.im.connector.constant.BaseConst;
import org.jaybill.fast.im.connector.constant.enums.PlatformEnum;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

@Slf4j
public class JwtUtilTest {

    @Test
    public void testCreateToken_Decode() {
        var bizId = "testBizId";
        var userId = "testUserId";
        var token = JwtUtil.createToken(bizId, userId, PlatformEnum.Web, null);
        log.info("token = {}", token);
        var claims = JwtUtil.parseToken(token);
        var actualBizId = claims.getAudience().stream().toList().get(0);
        Assert.assertEquals(bizId, actualBizId);
        var actualUserId = claims.get(BaseConst.USER_ID, String.class);
        Assert.assertEquals(userId, actualUserId);
        var actualPlatform = claims.get(BaseConst.PLATFORM, String.class);
        Assert.assertEquals(PlatformEnum.Web.name(), actualPlatform);
        var actualTags = claims.get(BaseConst.TAGS, List.class);
        Assert.assertNotNull(actualTags);
        Assert.assertEquals(0, actualTags.size());
    }
}
