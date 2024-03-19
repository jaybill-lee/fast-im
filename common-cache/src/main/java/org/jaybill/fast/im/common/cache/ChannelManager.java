package org.jaybill.fast.im.common.cache;

import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.jaybill.fast.im.common.util.AssertUtil;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class ChannelManager {

    private final StatefulRedisClusterConnection<String, String> connection;

    public ChannelManager(StatefulRedisClusterConnection<String, String> connection) {
        this.connection = connection;
    }

    /**
     * add (bizId+userId, (channelId, ip:port))
     */
    public void addChannel(ConnectionKey key, String channelId, String serverAddress, int ttlMinutes) {
        AssertUtil.notNull(key);
        var bizId = key.getBizId();
        var userId = key.getUserId();
        AssertUtil.notNull(bizId);
        AssertUtil.notNull(userId);
        AssertUtil.isTrue(InetAddressValidator.getInstance().isValid(serverAddress));

        log.debug("add channel, key={}, id:{}, ip={}", key, channelId, serverAddress);
        var serverRedisKey = RedisKey.getUserConnectedServerKey(bizId, userId);
        connection.sync().hset(serverRedisKey, channelId, serverAddress);
        connection.sync().expire(serverRedisKey, Duration.ofMinutes(ttlMinutes));
    }

    /**
     * remove channelId from hash
     */
    public void removeChannel(ConnectionKey key, String channelId) {
        AssertUtil.notNull(key);
        var bizId = key.getBizId();
        var userId = key.getUserId();
        AssertUtil.notNull(bizId);
        AssertUtil.notNull(userId);

        log.debug("remove channel, key={}, id:{}", key, channelId);
        var serverRedisKey = RedisKey.getUserConnectedServerKey(bizId, userId);
        connection.sync().hdel(serverRedisKey, channelId);
    }

    /**
     * renewal expire time
     */
    public void renewalChannel(ConnectionKey key, int ttlMinutes) {
        AssertUtil.notNull(key);
        var bizId = key.getBizId();
        var userId = key.getUserId();
        AssertUtil.notNull(bizId);
        AssertUtil.notNull(userId);

        log.debug("renewal channel, key={}", key);
        var serverRedisKey = RedisKey.getUserConnectedServerKey(bizId, userId);
        connection.sync().expire(serverRedisKey, Duration.ofMinutes(ttlMinutes));
    }

    /**
     * @return (channelId, serverIp:port)
     */
    public Map<String, String> getServerAddress(String bizId, String userId) {
        if (StringUtils.isAnyEmpty(bizId, userId)) {
            return Collections.emptyMap();
        }
        return connection.sync().hgetall(RedisKey.getUserConnectedServerKey(bizId, userId));
    }

    /**
     * save offline messages
     */
    public void inOfflineQueue(String bizId, String userId, List<String> messages) {
        AssertUtil.notNull(bizId);
        AssertUtil.notNull(userId);
        if (CollectionUtils.isEmpty(messages)) {
            return;
        }
        var key = RedisKey.getUserOfflineQueue(bizId, userId);
        Long len = connection.sync().rpush(key, messages.toArray(new String[0]));
        log.debug("current queue len:{}", len);
        // Default retention for up to 30 days, with a maximum of 100 entries
        connection.sync().expire(key, Duration.ofDays(30));
        if (len != null && len > 100) {
            connection.sync().lpop(key, len - 100);
        }
    }
}
