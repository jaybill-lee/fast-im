package org.jaybill.fast.im.common.web.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.jaybill.fast.im.common.dto.Keys;
import org.jaybill.fast.im.common.util.JsonUtil;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JwtParseUtil {

    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String PRIVATE_KEY = "privateKey";
    public static final String PUBLIC_KEY = "publicKey";
    private static final String CSMS_FILE_TEMPLATE = "csms-{0}.json";
    private static final Map<String, Keys> CSMS_MAP = new HashMap<>();

    /**
     * It would parse the header and the payload of JWT, then validate the signature. <br/>
     * If the token is expired or the signature validate fail, it will throw exception. <br/>
     * @return contains:<br/>
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

    /**
     * get unmodifiable map
     */
    public static Map<String, Keys> getCsmsMap() {
        return Collections.unmodifiableMap(CSMS_MAP);
    }

    public static void loadCsmsFile(String profile) {
        var url = JwtParseUtil.class.getClassLoader().getResource("");
        assert url != null;
        var path = url.getPath();
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        var map = JsonUtil.fromJson(new File(path + MessageFormat.format(CSMS_FILE_TEMPLATE, profile)), Map.class);

        // update CSMS_MAP
        map.forEach((k, v) -> {
            var keys = new Keys();
            if (!(v instanceof Map)) {
                return;
            }

            var vMap = (Map) v;
            // 1. Public Key
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

            // 2. Private Key
            if (vMap.get(PRIVATE_KEY) != null) {
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
}
