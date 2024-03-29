package org.jaybill.fast.im.connector.ws.evt;

import lombok.*;

import java.util.concurrent.TimeUnit;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PushEvt {
    private String bizId;
    private String userId;
    private String message;
    /**
     * If it is true, it will wait for the acknowledgment from the other end;
     * otherwise, it will send and forget.
     */
    private boolean withAck;
    /**
     * if {@link PushEvt#withAck} is true, it takes effect.
     */
    private long timeout;
    private TimeUnit unit;
}
