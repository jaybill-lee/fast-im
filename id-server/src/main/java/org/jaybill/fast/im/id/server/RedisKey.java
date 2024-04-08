package org.jaybill.fast.im.id.server;

import java.text.MessageFormat;

public class RedisKey {

    private static final String INDEX = "ID:SRV:INDEX:{0}";

    public static String getIndex(String bizId) {
        return MessageFormat.format(INDEX, bizId);
    }
}
