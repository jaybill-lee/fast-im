package org.jaybill.fast.im.net.ws;

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
    private WebSocketListener interceptor;
    private List<ChannelHandler> handlers;
}
