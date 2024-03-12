package org.jaybill.fast.im.connector.ws;

import io.netty.channel.Channel;
import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ChannelWrapper {
    private Channel channel;
}
