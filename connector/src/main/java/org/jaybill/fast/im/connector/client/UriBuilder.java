package org.jaybill.fast.im.connector.client;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class UriBuilder {

    private String schema;
    private String address;
    private Integer port;
    private String path;
    private Map<String, String> params;

    public UriBuilder schema(String schema) {
        this.schema = schema;
        return this;
    }

    public UriBuilder address(String address) {
        if (address.contains(":")) {
            var items = address.split(":");
            this.address = items[0];
            this.port = Integer.parseInt(items[1]);
            return this;
        }
        this.address = address;
        return this;
    }

    public UriBuilder port(Integer port) {
        this.port = port;
        return this;
    }

    public UriBuilder path(String path) {
        this.path = path;
        return this;
    }

    public UriBuilder params(String k, String v) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put(k, v);
        return this;
    }

    public static UriBuilder builder() {
        return new UriBuilder();
    }

    public URI build() {
        var uri = new StringBuilder();
        if (schema == null) {
            uri.append("http");
        } else {
            uri.append(schema);
        }
        uri.append("://");
        if (StringUtils.isBlank(address)) {
            throw new IllegalArgumentException();
        }
        uri.append(address);
        if (port != null) {
            uri.append(":").append(port);
        }
        if (StringUtils.isNotBlank(path)) {
            if (path.startsWith("/")) {
                uri.append(path);
            } else {
                uri.append("/").append(path);
            }
        }
        if (MapUtils.isNotEmpty(params)) {
            uri.append("?");
            params.forEach((k, v) -> uri.append(k).append("=").append(v).append("&"));
        }
        if (uri.charAt(uri.length() - 1) == '&') {
            uri.deleteCharAt(uri.length() - 1);
        }
        return URI.create(uri.toString());
    }
}
