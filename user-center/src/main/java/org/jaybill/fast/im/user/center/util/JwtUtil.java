package org.jaybill.fast.im.user.center.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.jaybill.fast.im.common.util.IdUtil;
import org.jaybill.fast.im.common.web.util.JwtParseUtil;

import java.util.Date;

public class JwtUtil {

    public static final String ISSUER = "user-center";
    public static final String AUD = "id-server";

    public static String createServiceToken() {
        return Jwts.builder()
                .id(IdUtil.getUuid())
                .issuer(ISSUER)
                .audience().add(AUD).and()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 2))
                .signWith(SignatureAlgorithm.RS256, JwtParseUtil.getCsmsMap().get(ISSUER).getPrivateKey())
                .compact();
    }
}
