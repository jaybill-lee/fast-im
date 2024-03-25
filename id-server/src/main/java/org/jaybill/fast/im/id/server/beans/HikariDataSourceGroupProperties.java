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
@ConfigurationProperties(prefix = "fast.im.id.server.hikari.mysql")
public class HikariDataSourceGroupProperties {

    private boolean autoCommit = true;
    private int connectionTimeout = 30000; // 30s
    private int idleTimeout = 60000; // 60s
    private int keepaliveTime = 0; // disable
    private int maxLifeTime = 1800000; // 30min
    private int maximumPoolSize = 10;
    private List<Address> address;

    @Data
    public static class Address {
        private String jdbcUrl;
        private String username;
        private String password;
    }
}
