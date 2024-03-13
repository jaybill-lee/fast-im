package org.jaybill.fast.im.connector.ws.evt;

import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PushEvt {
    private String bizId;
    private String userId;
    private String text;
    private boolean includeRemoteChannel;
}
