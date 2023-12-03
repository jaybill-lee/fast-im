package org.jaybill.fast.im.connector.netty.dto;

import lombok.*;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class WsDataDTO {
    /**
     * 0: to users;
     * 1: to groups;
     */
    private int targetType;

    /**
     * userIds or groupIds
     */
    private Set<String> targets;

    /**
     * 0: common
     */
    private int textType;

    /**
     * sent text
     */
    private String text;

    /**
     * sender transparent data
     */
    private String extra;

    public void validate() {
    }
}
