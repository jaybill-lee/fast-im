package org.jaybill.fast.im.net.http;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HttpConfig {
    private HttpDispatcher dispatcher;
}
