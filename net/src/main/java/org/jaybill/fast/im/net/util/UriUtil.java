package org.jaybill.fast.im.net.util;

import lombok.Getter;
import org.jaybill.fast.im.common.util.AssertUtil;

import java.util.HashMap;
import java.util.Map;

public class UriUtil {

    @Getter
    public static class UriComponent {
        String path;
        Map<String, String> params;

        public UriComponent() {
            this.params = new HashMap<>();
        }
    }

    /**
     * uri -> UriComponent
     * @param rawPath uri
     */
    public static UriComponent resolveUri(String rawPath) {
        var uriComponent = new UriComponent();
        int index = rawPath.indexOf("?");
        if (index > 0) {
            uriComponent.path = rawPath.substring(0, index);
            if (rawPath.length() - 1 == index) {
                return uriComponent;
            }
            var rawParams = rawPath.substring(index + 1);
            var paramsArray = rawParams.split("\\&");
            for (var params : paramsArray) {
                var kv = params.split("\\=");
                if (kv.length == 2) {
                    uriComponent.params.put(kv[0], kv[1]);
                }
            }
        } else {
            uriComponent.path = rawPath;
        }
        return uriComponent;
    }

    /**
     * build server format string
     */
    public static String buildServerAddress(String ip, int port) {
        AssertUtil.notNull(ip);
        return ip + ":" + port;
    }
}
