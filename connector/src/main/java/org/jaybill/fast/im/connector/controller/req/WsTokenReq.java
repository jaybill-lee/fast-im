package org.jaybill.fast.im.connector.controller.req;

import lombok.*;

import java.util.List;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class WsTokenReq {
    private String userId;
    private List<String> tags;
}
