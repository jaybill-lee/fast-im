package org.jaybill.fast.im.connector.ws.message;

import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private String id;
    private String message;
}
