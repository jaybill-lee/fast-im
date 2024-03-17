package org.jaybill.fast.im.connector.ws.evt;

import lombok.*;
import org.jaybill.fast.im.connector.ws.message.Message;

import java.util.concurrent.TimeUnit;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class InternalPushEvt {
    private String bizId;
    private String userId;
    private Message message;
    /**
     * If it is true, it will wait for the acknowledgment from the other end;
     * otherwise, it will send and forget.
     */
    private boolean withAck;
    /**
     * if {@link PushEvt#isWithAck()} is true, it takes effect.
     */
    private long timeout;
    private TimeUnit unit;

    private boolean enableRemotePush;
}
