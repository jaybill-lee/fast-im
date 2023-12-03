package org.jaybill.fast.im.common.cache.spring;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ToString
@ConfigurationProperties(prefix = "fast.im.common.cache.redis")
public class RedisProperties {
    /**
     * host1:port1,host2:port2
     */
    private String address;
}
