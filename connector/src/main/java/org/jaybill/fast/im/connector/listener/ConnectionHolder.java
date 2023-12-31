package org.jaybill.fast.im.connector.listener;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.jaybill.fast.im.connector.constant.BaseConst;
import org.jaybill.fast.im.connector.constant.enums.PlatformEnum;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class ConnectionHolder {

    /**
     * key: bizId_userId_platform
     */
    private final Map<String, ChannelWrapper> channelMap;

    /**
     * key: bizId <br/>
     * subKey: userId <br/>
     */
    private final Map<String, Map<String, Set<ChannelWrapper>>> bizId2UserChannelsMap;
    private final Map<ChannelWrapper, Set<ChannelWrapper>> channel2UserChannelsMap;

    /**
     * key: bizId <br/>
     * subKey: tag <br/>
     */
    private final Map<String, Map<String, Set<ChannelWrapper>>> bizId2TagChannelsMap;
    private final Map<ChannelWrapper, Set<ChannelWrapper>> channel2TagChannelsMap;

    public ConnectionHolder() {
        this.channelMap = new ConcurrentHashMap<>();
        this.bizId2UserChannelsMap = new ConcurrentHashMap<>();
        this.channel2UserChannelsMap = new ConcurrentHashMap<>();
        this.bizId2TagChannelsMap = new ConcurrentHashMap<>();
        this.channel2TagChannelsMap = new ConcurrentHashMap<>();
    }

    public void hold(ChannelWrapper channelWrapper) {
        var channel = channelWrapper.getChannel();
        var bizId = (String) channel.attr(AttributeKey.valueOf(BaseConst.BIZ_ID)).get();
        var userId = (String) channel.attr(AttributeKey.valueOf(BaseConst.USER_ID)).get();
        var platform = (PlatformEnum) channel.attr(AttributeKey.valueOf(BaseConst.PLATFORM)).get();
        var tags = (List) channel.attr(AttributeKey.valueOf(BaseConst.TAGS)).get();

        channelMap.put(bizId + userId + platform.name(), channelWrapper);

        var userChannelsMap = bizId2UserChannelsMap.computeIfAbsent(bizId, (k) -> new ConcurrentHashMap<>());
        var userChannels = userChannelsMap.computeIfAbsent(userId, (k) -> new CopyOnWriteArraySet<>());
        userChannels.add(channelWrapper);
        channel2UserChannelsMap.putIfAbsent(channelWrapper, userChannels);

        tags.forEach(tagObj -> {
            var tag = (String) tagObj;
            var tagChannelsMap = bizId2TagChannelsMap.computeIfAbsent(bizId, (k) -> new ConcurrentHashMap<>());
            var tagChannels = tagChannelsMap.computeIfAbsent(tag, (k) -> new CopyOnWriteArraySet<>());
            tagChannels.add(channelWrapper);
            channel2TagChannelsMap.putIfAbsent(channelWrapper, tagChannels);
        });
    }

    public void remove(String bizId, String userId, String platform) {
        var channelWrapper = channelMap.remove(bizId + userId + platform);
        if (channelWrapper == null) {
            return;
        }
        var userChannels = channel2UserChannelsMap.get(channelWrapper);
        if (userChannels != null) {
            userChannels.remove(channelWrapper);
        }
        var tagChannels = channel2TagChannelsMap.get(channelWrapper);
        if (tagChannels != null) {
            tagChannels.remove(channelWrapper);
        }
    }

    public void removeThenHold(String bizId, String userId, String platform, Channel channel) {
        this.remove(bizId, userId, platform);
        this.hold(new ChannelWrapper(channel));
    }
}
