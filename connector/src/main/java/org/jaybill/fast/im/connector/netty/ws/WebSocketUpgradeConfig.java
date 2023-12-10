package org.jaybill.fast.im.connector.netty.ws;

import io.netty.channel.ChannelHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketUpgradeConfig {
    private String path;
    private WebSocketUpgradeInterceptor interceptor;
    private List<ChannelHandler> handlers;
}
