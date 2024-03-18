package org.jaybill.fast.im.net.spring.properties;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * WebSocket config
 */
@Data
@ToString
@ConfigurationProperties(prefix = "fast.im.net.spring.ws")
public class WebSocketProperties {
    /**
     * such as /ws
     */
    private String path;

    /**
     * max idle seconds before close channel
     */
    private int idleSeconds;
}
