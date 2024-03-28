package org.jaybill.fast.im.id.server.beans;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * see
 * <a href="https://github.com/brettwooldridge/HikariCP?tab=readme-ov-file#gear-configuration-knobs-baby">HikariCP Configuration</a>
 */
@Data
@Component
@ConfigurationProperties(prefix = "fast.im.id.server.maria.mysql")
public class MariaPoolDataSourceGroupProperties {

    private Boolean autoCommit = true;
    private Integer connectionTimeout = 30000; // 30s
    private Integer maxPoolSize = 8;
    private Integer minPoolSize = 8;
    private Integer poolValidMinDelay = 1000; // 1s
    private Integer maxIdleTime = 600; // 600s
    private List<Address> address;

    @Data
    public static class Address {
        private String poolName;
        private String address; // ip:port
        private String database;
        private String user;
        private String password;
    }
}
