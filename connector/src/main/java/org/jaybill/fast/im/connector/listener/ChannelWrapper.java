package org.jaybill.fast.im.connector.listener;

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
