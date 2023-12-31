package org.jaybill.fast.im.connector.listener.evt;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.jaybill.fast.im.connector.constant.enums.PlatformEnum;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Evt {
    private String id;
    private String bizId;
    private String userId;
    private PlatformEnum platform;
    private Channel channel;
}
