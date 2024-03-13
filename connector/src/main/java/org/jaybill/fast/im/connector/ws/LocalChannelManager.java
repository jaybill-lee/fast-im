package org.jaybill.fast.im.connector.ws;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.jaybill.fast.im.connector.constant.BaseConst;
import org.jaybill.fast.im.connector.constant.enums.PlatformEnum;
import org.jaybill.fast.im.connector.util.ChannelUtil;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
public class LocalChannelManager {

    private static final String DELIMITER = ":";

    /**
     * key - channelId
     */
    private final Map<String, Channel> id2ChannelMap;

    /**
     * key - bizId
     */
    private final Map<String, Set<Channel>> bizId2ChannelsMap;

    /**
     * key - bizId:userId
     */
    private final Map<String, Set<Channel>> bizIdAndUserId2ChannelsMap;

    /**
     * key - bizId:platform
     */
    private final Map<String, Set<Channel>> bizIdAndPlatform2ChannelsMap;

    /**
     * key - bizId:userId:platform
     */
    private final Map<String, Set<Channel>> bizIdAndUserIdAndPlatform2ChannelsMap;


    /**
     * key: bizId <br/>
     * subKey: userId
     */
    private final Map<String, Map<String, Set<Channel>>> bizId2UserId2ChannelsMap;

    /**
     * key: bizId <br/>
     * subKey: platform
     */
    private final Map<String, Map<String, Set<Channel>>> bizId2Platform2ChannelsMap;

    /**
     * key: bizId <br/>
     * subKey: tag <br/>
     */
    private final Map<String, Map<String, Set<Channel>>> bizId2Tag2ChannelsMap;

    public LocalChannelManager() {
        this.id2ChannelMap = new ConcurrentHashMap<>();
        this.bizId2ChannelsMap = new ConcurrentHashMap<>();
        this.bizIdAndUserId2ChannelsMap = new ConcurrentHashMap<>();
        this.bizIdAndPlatform2ChannelsMap = new ConcurrentHashMap<>();
        this.bizIdAndUserIdAndPlatform2ChannelsMap = new ConcurrentHashMap<>();
        this.bizId2Platform2ChannelsMap = new ConcurrentHashMap<>();
        this.bizId2UserId2ChannelsMap = new ConcurrentHashMap<>();
        this.bizId2Tag2ChannelsMap = new ConcurrentHashMap<>();
    }

    /**
     * Add a channel to manager
     * @param channel
     */
    public void addChannel(Channel channel) {
        var bizId = (String) channel.attr(AttributeKey.valueOf(BaseConst.BIZ_ID)).get();
        var userId = (String) channel.attr(AttributeKey.valueOf(BaseConst.USER_ID)).get();
        var platform = (PlatformEnum) channel.attr(AttributeKey.valueOf(BaseConst.PLATFORM)).get();
        var tags = (List<String>) channel.attr(AttributeKey.valueOf(BaseConst.TAGS)).get();

        var channelId = ChannelUtil.getId(channel);
        log.debug("add channel id:{}", channelId);
        id2ChannelMap.put(channelId, channel);
        bizId2ChannelsMap.computeIfAbsent(bizId, s -> new CopyOnWriteArraySet<>()).add(channel);
        bizIdAndUserId2ChannelsMap.computeIfAbsent(
                this.buildKey(bizId, userId),
                s -> new CopyOnWriteArraySet<>()
        ).add(channel);
        bizIdAndPlatform2ChannelsMap.computeIfAbsent(
                this.buildKey(bizId, platform.name()),
                s -> new CopyOnWriteArraySet<>()
        ).add(channel);
        bizIdAndUserIdAndPlatform2ChannelsMap.computeIfAbsent(
                this.buildKey(bizId, userId, platform.name()),
                s -> new CopyOnWriteArraySet<>()
        ).add(channel);
        bizId2UserId2ChannelsMap.computeIfAbsent(bizId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(userId, s -> new CopyOnWriteArraySet<>())
                .add(channel);
        bizId2Platform2ChannelsMap.computeIfAbsent(bizId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(platform.name(), s -> new CopyOnWriteArraySet<>())
                .add(channel);
        tags.forEach(tag ->
                bizId2Tag2ChannelsMap.computeIfAbsent(bizId, k -> new ConcurrentHashMap<>())
                        .computeIfAbsent(tag, s -> new CopyOnWriteArraySet<>())
                        .add(channel)
        );
    }

    /**
     * Remove a channel from manager
     * @param channel
     */
    public void removeChannel(Channel channel) {
        var bizId = (String) channel.attr(AttributeKey.valueOf(BaseConst.BIZ_ID)).get();
        var userId = (String) channel.attr(AttributeKey.valueOf(BaseConst.USER_ID)).get();
        var platform = (PlatformEnum) channel.attr(AttributeKey.valueOf(BaseConst.PLATFORM)).get();
        var tags = (List<String>) channel.attr(AttributeKey.valueOf(BaseConst.TAGS)).get();

        id2ChannelMap.remove(ChannelUtil.getId(channel));

        var channelSet = bizId2ChannelsMap.get(bizId);
        this.removeChannel(channelSet, channel);

        channelSet = bizIdAndUserId2ChannelsMap.get(this.buildKey(bizId, userId));
        this.removeChannel(channelSet, channel);

        channelSet = bizIdAndPlatform2ChannelsMap.get(this.buildKey(bizId, platform.name()));
        this.removeChannel(channelSet, channel);

        channelSet = bizIdAndUserIdAndPlatform2ChannelsMap.get(this.buildKey(bizId, userId, platform.name()));
        this.removeChannel(channelSet, channel);

        var bizMap = bizId2UserId2ChannelsMap.get(bizId);
        if (bizMap != null) {
            channelSet = bizMap.get(userId);
            this.removeChannel(channelSet, channel);
        }

        bizMap = bizId2Platform2ChannelsMap.get(bizId);
        if (bizMap != null) {
            channelSet = bizMap.get(userId);
            this.removeChannel(channelSet, channel);
        }

        bizMap = bizId2Tag2ChannelsMap.get(bizId);
        if (bizMap != null) {
            for (var tag : tags) {
                channelSet = bizMap.get(tag);
                this.removeChannel(channelSet, channel);
            }
        }
    }

    /**
     * Get all channels of bizId + userId
     */
    public Set<Channel> getChannels(String bizId, String userId) {
        var channels = bizIdAndUserId2ChannelsMap.get(this.buildKey(bizId, userId));
        if (channels == null) {
            return Collections.emptySet();
        }
        return channels;
    }

    private String buildKey(String... keys) {
        return String.join(DELIMITER, keys);
    }

    private void removeChannel(Set<Channel> channelSet, Channel channel) {
        if (channelSet == null) return;
        channelSet.remove(channel);
    }
}
