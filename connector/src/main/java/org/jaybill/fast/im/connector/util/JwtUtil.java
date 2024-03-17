package org.jaybill.fast.im.connector.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.jaybill.fast.im.common.dto.Keys;
import org.jaybill.fast.im.common.util.AssertUtil;
import org.jaybill.fast.im.common.util.IdUtil;
import org.jaybill.fast.im.common.util.JsonUtil;
import org.jaybill.fast.im.connector.constant.ChannelBaseConst;
import org.jaybill.fast.im.connector.constant.enums.PlatformEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class JwtUtil {

    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String PRIVATE_KEY = "privateKey";
    public static final String PUBLIC_KEY = "publicKey";
    public static final String ISSUER = "connector";
    private static final Map<String, Keys> CSMS_MAP = new HashMap<>();

    /*
     * init JWT signature public/private key
     */
    static {
        var url = JwtUtil.class.getClassLoader().getResource("");
        assert url != null;
        var path = url.getPath();
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        var map = JsonUtil.fromJson(new File(path + "csms.json"), Map.class);
        map.forEach((k, v) -> {
            var keys = new Keys();
            if (v instanceof Map vMap) {
                // Public Key
                var publicKeyPem = vMap.get(PUBLIC_KEY).toString()
                        .replaceAll("\\n", "")
                        .replace("-----BEGIN PUBLIC KEY-----", "")
                        .replace("-----END PUBLIC KEY-----", "");
                var x509KeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyPem));
                try {
                    var keyFactory = KeyFactory.getInstance("RSA");
                    var publicKey = keyFactory.generatePublic(x509KeySpec);
                    keys.setPublicKey(publicKey);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                // Private Key
                var privateKeyPem = vMap.get(PRIVATE_KEY).toString()
                        .replaceAll("\\n", "")
                        .replace("-----BEGIN PRIVATE KEY-----", "")
                        .replace("-----END PRIVATE KEY-----", "");
                var decodedPrivateKeyBytes = Base64.getDecoder().decode(privateKeyPem);
                PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(decodedPrivateKeyBytes);
                try {
                    var keyFactory = KeyFactory.getInstance("RSA");
                    var privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
                    keys.setPrivateKey(privateKey);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            CSMS_MAP.put(k.toString(), keys);
        });
    }

    public static String createInternalServiceToken() {
        return Jwts.builder()
                .id(IdUtil.getUuid())
                .issuer(ISSUER)
                .audience().add(ISSUER).and()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 2))
                .signWith(SignatureAlgorithm.RS256, CSMS_MAP.get(ISSUER).getPrivateKey())
                .compact();
    }

    public static String createWebsocketToken(String bizId, String userId, PlatformEnum platform, List<String> tags) {
        AssertUtil.notNull(bizId);
        AssertUtil.notNull(userId);
        tags = tags == null ? new ArrayList<>() : tags;
        return Jwts.builder()
                .id(IdUtil.getUuid())
                .issuer(ISSUER)
                .audience().add(ISSUER).and()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 2))
                .signWith(SignatureAlgorithm.RS256, CSMS_MAP.get(ISSUER).getPrivateKey())
                .claim(ChannelBaseConst.USER_ID, userId)
                .claim(ChannelBaseConst.PLATFORM, platform.name())
                .claim(ChannelBaseConst.BIZ_ID, bizId)
                .claim(ChannelBaseConst.TAGS, tags)
                .compact();
    }

    /**
     * It would parse the header and the payload of JWT, then validate the signature. <br/>
     * If the token is expired or the signature validate fail, it will throw exception. <br/>
     * @return contains:<br/>
     * @see JwtUtil#createWebsocketToken(String, String, PlatformEnum, List)
     */
    public static Claims parseToken(String token) {
        if (token.startsWith(BEARER)) {
            token = token.replace(BEARER, "");
        }

        // get public key from JWT Payload
        String [] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException();
        }
        var payload = new String(Base64.getDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        var payloadMap = JsonUtil.fromJson(payload, Map.class);
        var issuer = payloadMap.get("iss").toString();
        var publicKey = CSMS_MAP.get(issuer).getPublicKey();

        // do parse
        return Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token).getPayload();
    }
}
