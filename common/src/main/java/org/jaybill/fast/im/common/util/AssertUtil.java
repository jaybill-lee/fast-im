package org.jaybill.fast.im.common.util;

import org.jaybill.fast.im.common.exception.BaseException;

public class AssertUtil {

    public static void notNull(Object o) {
        if (o == null) {
            throw new BaseException();
        }
    }

    public static void isTrue(boolean t) {
        if (!t) {
            throw new BaseException();
        }
    }
}
