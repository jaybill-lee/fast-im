package org.jaybill.fast.im.connector.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.jaybill.fast.im.common.util.AssertUtil;
import org.jaybill.fast.im.common.util.IdUtil;
import org.jaybill.fast.im.common.web.util.JwtParseUtil;
import org.jaybill.fast.im.connector.constant.ChannelBaseConst;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class JwtUtil {
    public static final String ISSUER = "connector";
    public static final String WS_AUD = "wsClient";

    public static String createInternalServiceToken() {
        return Jwts.builder()
                .id(IdUtil.getUuid())
                .issuer(ISSUER)
                .audience().add(ISSUER).and()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 2))
                .signWith(SignatureAlgorithm.RS256, JwtParseUtil.getCsmsMap().get(ISSUER).getPrivateKey())
                .compact();
    }

    public static String createWebsocketToken(String bizId, String userId, List<String> tags) {
        AssertUtil.notNull(bizId);
        AssertUtil.notNull(userId);
        tags = tags == null ? new ArrayList<>() : tags;
        return Jwts.builder()
                .id(IdUtil.getUuid())
                .issuer(ISSUER)
                .audience().add(WS_AUD).and()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 2))
                .signWith(SignatureAlgorithm.RS256, JwtParseUtil.getCsmsMap().get(ISSUER).getPrivateKey())
                .claim(ChannelBaseConst.USER_ID, userId)
                .claim(ChannelBaseConst.BIZ_ID, bizId)
                .claim(ChannelBaseConst.TAGS, tags)
                .compact();
    }
}
