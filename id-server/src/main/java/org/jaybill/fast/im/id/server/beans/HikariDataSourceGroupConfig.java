package org.jaybill.fast.im.id.server.beans;

import com.zaxxer.hikari.HikariDataSource;
import org.mariadb.jdbc.Driver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HikariDataSourceGroupConfig {

    @Bean
    public HikariDataSource hikariDataSource() {
        var ds = new HikariDataSource();
        ds.setDriverClassName(Driver.class.getName());

        return ds;
    }
}
