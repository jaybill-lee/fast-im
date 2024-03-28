package org.jaybill.fast.im.id.server.beans;

import lombok.extern.slf4j.Slf4j;
import org.jaybill.fast.im.id.server.MariaPoolDataSourceGroup;
import org.mariadb.jdbc.MariaDbPoolDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;

import java.sql.SQLException;

@Slf4j
@Configuration
public class MariaPoolDataSourceGroupConfig {

    @Bean
    public MariaPoolDataSourceGroup mariaPoolDataSourceGroup(MariaPoolDataSourceGroupProperties properties) {
        var group = new MariaPoolDataSourceGroup();
        properties.getAddress().forEach(address -> {
            var jdbcUrlBuilder = new StringBuilder("jdbc:mariadb://");
            jdbcUrlBuilder
                    .append(address.getAddress())
                    .append("/")
                    .append(address.getDatabase())
                    .append("?")
                    .append("user=")
                    .append(address.getUser())
                    .append("&")
                    .append("password=")
                    .append(address.getPassword())
                    .append("&")
            ;

            if (address.getPoolName() != null) {
                jdbcUrlBuilder.append("poolName=").append(address.getPoolName()).append("&");
            }

            ReflectionUtils.doWithFields(MariaPoolDataSourceGroupProperties.class, field -> {
                field.setAccessible(true);
                var val = field.get(properties);
                if (val != null) {
                    var name = field.getName();
                    jdbcUrlBuilder.append(name).append("=").append(val).append("&");
                }
            }, field -> !field.getName().equals("address"));
            jdbcUrlBuilder.deleteCharAt(jdbcUrlBuilder.length() - 1);

            var jdbcUrl = jdbcUrlBuilder.toString();
            log.debug("jdbc url: {}", jdbcUrl);
            try {
                var pool = new MariaDbPoolDataSource(jdbcUrl);
                group.addDataSource(pool);
            } catch (SQLException e) {
                log.error("create maria pool fail:", e);
                throw new RuntimeException(e);
            }
        });
        return group;
    }
}
