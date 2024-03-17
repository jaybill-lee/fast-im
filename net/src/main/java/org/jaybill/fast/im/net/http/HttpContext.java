package org.jaybill.fast.im.net.http;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpContext {

    private final Map<String, Object> ctx;

    public HttpContext() {
        this.ctx = new ConcurrentHashMap<>();
    }

    public Object put(String k, Object v) {
        return this.ctx.put(k, v);
    }

    public Object remove(String k) {
        return this.ctx.remove(k);
    }

    public Object get(String key) {
        return this.ctx.get(key);
    }
}
