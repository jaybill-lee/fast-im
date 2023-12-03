package org.jaybill.fast.im.connector.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.jaybill.fast.im.common.util.AssertUtil;
import org.jaybill.fast.im.common.util.IdUtil;
import org.jaybill.fast.im.connector.constant.BaseConst;
import org.jaybill.fast.im.connector.constant.enums.PlatformEnum;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class JwtUtil {

    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";

    private static final String ISSUER = "fast-im";
    private static final String SECRET = "Igy2WowUngSORtZgK8bmhij1uV7zqwa+VinI8PiPqps=";
    private static final String BASE64_SECRET = Base64.getEncoder()
            .encodeToString(SECRET.getBytes(StandardCharsets.UTF_8));

    public static String createToken(String bizId, String userId, PlatformEnum platform, List<String> tags) {
        AssertUtil.notNull(bizId);
        AssertUtil.notNull(userId);
        tags = tags == null ? new ArrayList<>() : tags;
        return Jwts.builder()
                .id(IdUtil.getUuid())
                .issuer(ISSUER)
                .audience().add("ws-login").and()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 2))
                .signWith(SignatureAlgorithm.HS256, BASE64_SECRET)
                .claim(BaseConst.BIZ_ID, bizId)
                .claim(BaseConst.USER_ID, userId)
                .claim(BaseConst.PLATFORM, platform.name())
                .claim(BaseConst.TAGS, tags)
                .compact();
    }

    /**
     * @return contains:<br/>
     * 1. bizId: xxx <br/>
     * 2. tags: {k1:v1, k2:v2} <br/>
     */
    public static Claims parseToken(String token) {
        if (token.startsWith(BEARER)) {
            token = token.replace(BEARER, "");
        }
        return Jwts.parser().setSigningKey(BASE64_SECRET).build()
                .parseClaimsJws(token)
                .getBody();
    }
}
