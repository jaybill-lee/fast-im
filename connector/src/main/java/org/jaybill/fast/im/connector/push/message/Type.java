package org.jaybill.fast.im.connector.push.message;

public enum Type {
    /**
     * System message. Supported placeholders, such as:<br/>
     * - ${userId:1223}
     */
    System,
    User
}
