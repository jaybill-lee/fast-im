package org.jaybill.fast.im.connector.beans;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "fast.im.connector")
public class ConnectorProperties {

    /**
     * http port
     */
    private int httpPort;

    /**
     * tcp port
     */
    private int port;
}
