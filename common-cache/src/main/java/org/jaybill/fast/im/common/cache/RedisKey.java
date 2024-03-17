package org.jaybill.fast.im.common.cache;

import java.text.MessageFormat;

public class RedisKey {
    private static final String USER_CONNECTED_SERVER_KEY = "user:conn:svr:{0}:{1}";
    private static final String USER_OFFLINE_QUEUE = "user:offline:queue:{0}:{1}";

    public static String getUserConnectedServerKey(String bizId, String userId) {
        return MessageFormat.format(USER_CONNECTED_SERVER_KEY, bizId, userId);
    }

    public static String getUserOfflineQueue(String bizId, String userId) {
        return MessageFormat.format(USER_OFFLINE_QUEUE, bizId, userId);
    }
}
