package org.jaybill.fast.im.connector.controller.req;

import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SimplePushReq {
    private String userId;
    private String text;
}
