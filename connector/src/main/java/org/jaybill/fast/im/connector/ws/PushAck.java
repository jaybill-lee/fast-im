package org.jaybill.fast.im.connector.ws;

import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PushAck {
    private String channelId;
    private String messageId;
}
