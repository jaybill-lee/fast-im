package org.jaybill.fast.im.common.util;

import java.util.UUID;

public class IdUtil {

    public static String getUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
