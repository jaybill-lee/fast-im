package org.jaybill.fast.im.connector.ws;

import lombok.*;
import org.jaybill.fast.im.connector.ws.evt.PushEvt;

import java.util.List;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PushResult {
    /**
     * push id
     */
    private String id;

    /**
     * if {@link PushEvt#isWithAck()} return true, then all channels that return an ack will be added here.
     * Otherwise, it will be an empty list.
     */
    private List<String> ackChannelIds;
}
