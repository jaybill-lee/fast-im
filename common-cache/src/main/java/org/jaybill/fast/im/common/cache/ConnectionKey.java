package org.jaybill.fast.im.common.cache;

import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionKey {
    private String bizId;
    private String userId;
    private String platform;
}
