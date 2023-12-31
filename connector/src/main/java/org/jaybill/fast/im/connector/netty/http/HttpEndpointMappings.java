package org.jaybill.fast.im.connector.netty.http;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpEndpointMappings {
    private static final Map<String, Map<String, MethodDefinition>> METHOD_DEFINITION_MAP = new ConcurrentHashMap<>();

    /**
     * save mapping
     * @param path             http path
     * @param method           {@link HttpMethods}
     * @param methodDefinition
     */
    public static void set(String path, String method, MethodDefinition methodDefinition) {
        METHOD_DEFINITION_MAP.compute(path, (k, v) -> {
            if (v == null) {
                var innerMap = new ConcurrentHashMap<String, MethodDefinition>();
                innerMap.put(method, methodDefinition);
                return innerMap;
            } else {
                v.compute(method, (x, y) -> {
                    if (y == null) {
                        return methodDefinition;
                    } else {
                        throw new IllegalArgumentException();
                    }
                });
                return v;
            }
        });
    }

    public static MethodDefinition get(String path, String method) {
        var map = METHOD_DEFINITION_MAP.get(path);
        if (map == null) {
            return null;
        }
        return map.get(method);
    }
}
