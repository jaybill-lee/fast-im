package org.jaybill.fast.im.net.spring.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Base net config
 */
@Data
@ConfigurationProperties(prefix = "fast.im.connector")
public class ConnectorProperties {

    /**
     * http port
     */
    private int httpPort;
}
