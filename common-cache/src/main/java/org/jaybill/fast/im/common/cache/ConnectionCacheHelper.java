package org.jaybill.fast.im.common.cache;

import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.jaybill.fast.im.common.util.AssertUtil;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

@Slf4j
public class ConnectionCacheHelper {

    private final StatefulRedisClusterConnection<String, String> connection;

    public ConnectionCacheHelper(StatefulRedisClusterConnection<String, String> connection) {
        this.connection = connection;
    }

    public void bind(ConnectionKey key, String serverIp) {
        AssertUtil.notNull(key);
        var bizId = key.getBizId();
        var userId = key.getUserId();
        var platform = key.getPlatform();
        AssertUtil.notNull(bizId);
        AssertUtil.notNull(userId);
        AssertUtil.notNull(platform);
        AssertUtil.isTrue(InetAddressValidator.getInstance().isValid(serverIp));

        var redisKey = RedisKey.getUserConnectedServerKey(bizId, userId);
        log.debug("bind connection, key={}, ip={}", redisKey, serverIp);
        connection.sync().hset(redisKey, platform, serverIp);
        connection.sync().expire(redisKey, Duration.ofMinutes(30));
    }

    public Map<String, String> getIp(String bizId, String userId) {
        if (StringUtils.isAnyEmpty(bizId, userId)) {
            return Collections.emptyMap();
        }
        return connection.sync().hgetall(RedisKey.getUserConnectedServerKey(bizId, userId));
    }

    public String getIp(ConnectionKey key) {
        var bizId = key.getBizId();
        var userId = key.getUserId();
        var platform = key.getPlatform();
        if (StringUtils.isAnyEmpty(bizId, userId, platform)) {
            return null;
        }
        return connection.sync().hget(RedisKey.getUserConnectedServerKey(bizId, userId), platform);
    }
}
