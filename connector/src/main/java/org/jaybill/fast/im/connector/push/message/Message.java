package org.jaybill.fast.im.connector.push.message;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Message {
    /**
     * define the detail of interaction with client-side
     */
    private Type type;

    /**
     * Always userId.
     */
    private String from;

    private String message;
}
