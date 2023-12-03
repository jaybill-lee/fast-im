package org.jaybill.fast.im.connector.constant.enums;

import java.util.Arrays;

public enum PlatformEnum {
    Web,
    Mobile,
    PC;

    public static PlatformEnum fromName(String name) {
        return Arrays.stream(PlatformEnum.values())
                .filter(p -> p.name().equals(name))
                .findAny()
                .orElse(null);
    }
}
