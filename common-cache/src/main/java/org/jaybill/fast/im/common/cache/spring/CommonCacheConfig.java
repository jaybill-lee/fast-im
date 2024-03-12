package org.jaybill.fast.im.common.cache.spring;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisURI;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.event.DefaultEventPublisherOptions;
import io.lettuce.core.event.Event;
import io.lettuce.core.event.metrics.CommandLatencyEvent;
import io.lettuce.core.metrics.CommandLatencyId;
import io.lettuce.core.metrics.CommandMetrics;
import io.lettuce.core.output.StatusOutput;
import io.lettuce.core.protocol.Command;
import io.lettuce.core.protocol.CommandType;
import io.lettuce.core.protocol.ProtocolVersion;
import io.lettuce.core.protocol.RedisCommand;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.Delay;
import io.lettuce.core.resource.NettyCustomizer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.commons.lang3.StringUtils;
import org.jaybill.fast.im.common.cache.ChannelManager;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Configuration
@EnableConfigurationProperties(value = RedisProperties.class)
public class CommonCacheConfig {

    @Bean
    public RedisClusterClient redisClusterClient(RedisProperties properties) {

        // 1. Create cluster client
        var clusterClient = RedisClusterClient.create(this.buildClientResources(),
                this.buildSeedRedisUris(properties.getAddress()));
        // 2. Config cluster client options
        clusterClient.setOptions(this.buildClientOptions());
        // 3. Config client eventBus listener
        clusterClient.getResources().eventBus().get().subscribe(new DefaultEvtHandler());
        return clusterClient;
    }

    @Bean
    public StatefulRedisClusterConnection<String, String> statefulRedisClusterConnection(RedisClusterClient client) {
        return client.connect();
    }

    @Bean
    public ChannelManager connectionCacheHelper(StatefulRedisClusterConnection<String, String> connection) {
        return new ChannelManager(connection);
    }

    private List<RedisURI> buildSeedRedisUris(String addressStr) {
        String[] addressArray = StringUtils.split(addressStr, ",");
        var seedHostList = new ArrayList<RedisURI>();
        for (var address : addressArray) {
            String [] items = StringUtils.split(address, ":");
            seedHostList.add(RedisURI.builder().withHost(items[0]).withPort(Integer.parseInt(items[1])).build());
        }
        return seedHostList;
    }

    private ClientResources buildClientResources() {
        return ClientResources.builder()
                .ioThreadPoolSize(Runtime.getRuntime().availableProcessors())
                .computationThreadPoolSize(Runtime.getRuntime().availableProcessors())
                .nettyCustomizer(new NettyCustomizer() {
                    @Override
                    public void afterChannelInitialized(Channel channel) {
                        channel.pipeline().addLast("idleHandler", new IdleStateHandler(0, 0, 15));
                        channel.pipeline().addLast("redisPingHandler", new ChannelDuplexHandler() {
                            @Override
                            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
                                if (!(evt instanceof IdleStateEvent)) {
                                    return;
                                }
                                ctx.channel().writeAndFlush(new Command<>(
                                        CommandType.PING,
                                        new StatusOutput<>(StringCodec.UTF8)
                                ));
                            }
                        });
                    }
                })
                .reconnectDelay(Delay.exponential())
                .commandLatencyPublisherOptions(DefaultEventPublisherOptions.builder()
                        .eventEmitInterval(Duration.ofSeconds(10))
                        .build())
                .build();
    }

    private ClusterClientOptions buildClientOptions() {
        return ClusterClientOptions.builder()
                .autoReconnect(true)
                .maxRedirects(5)
                .pingBeforeActivateConnection(true)
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.DEFAULT)
                .protocolVersion(ProtocolVersion.RESP3)
                .requestQueueSize(Integer.MAX_VALUE)
                .scriptCharset(StandardCharsets.UTF_8)
                .socketOptions(SocketOptions.builder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .keepAlive(true)
                        .tcpNoDelay(true).build())
                .suspendReconnectOnProtocolFailure(false)
                .timeoutOptions(TimeoutOptions.builder()
                        .timeoutCommands(true)
                        .timeoutSource(new TimeoutOptions.TimeoutSource() {
                            @Override
                            public long getTimeout(RedisCommand<?, ?, ?> command) {
                                if (command.getType().equals(CommandType.SCAN) || command.getType().equals(CommandType.EVAL)) {
                                    return 10;
                                }
                                return 5;
                            }
                            @Override
                            public TimeUnit getTimeUnit() {
                                return TimeUnit.SECONDS;
                            }
                        }).build())
                .topologyRefreshOptions(ClusterTopologyRefreshOptions.builder()
                        .enableAdaptiveRefreshTrigger(
                                ClusterTopologyRefreshOptions.RefreshTrigger.ASK_REDIRECT,
                                ClusterTopologyRefreshOptions.RefreshTrigger.MOVED_REDIRECT,
                                ClusterTopologyRefreshOptions.RefreshTrigger.PERSISTENT_RECONNECTS,
                                ClusterTopologyRefreshOptions.RefreshTrigger.UNCOVERED_SLOT,
                                ClusterTopologyRefreshOptions.RefreshTrigger.UNKNOWN_NODE
                        )
                        .adaptiveRefreshTriggersTimeout(Duration.ofSeconds(10))
                        .enablePeriodicRefresh(true)
                        .enablePeriodicRefresh(Duration.ofMinutes(1))
                        .closeStaleConnections(true)
                        .dynamicRefreshSources(true)
                        .build())
                .validateClusterNodeMembership(true)
                .build();
    }

    private static class DefaultEvtHandler implements Consumer<Event> {

        @Override
        public void accept(Event event) {
            if (event instanceof CommandLatencyEvent evt) {
                Map<CommandLatencyId, CommandMetrics> latencies = evt.getLatencies();
                latencies.forEach((id, metrics) -> {
                    String commandName = id.commandType().name();
                    String endpoint = id.remoteAddress().toString();
                    long cnt = metrics.getCount();
                    TimeUnit unit = metrics.getTimeUnit();

                    CommandMetrics.CommandLatency first = metrics.getFirstResponse();
                    // tp50、tp90
                    Map<Double, Long> firstPercentiles = first.getPercentiles();
                    long firstMin = first.getMin();
                    long firstMax = first.getMax();


                    CommandMetrics.CommandLatency completion = metrics.getCompletion();
                    // tp50、tp90
                    Map<Double, Long> completionPercentiles = completion.getPercentiles();
                    long completionMin = completion.getMin();
                    long completionMax = completion.getMax();

                    System.out.printf("{%s:%s, %s:%s, %s:%s, %s:%s, %s:%s, %s:%s}%n",
                            "\"commandName\"", "\"" + commandName + "\"",
                            "\"unit\"", "\"" + unit.name() + "\"",
                            "\"cnt\"", "\"" + cnt + "\"",
                            "\"endpoint\"", "\"" + endpoint + "\"",
                            "\"first\"", String.format("{%s:%s, %s:%s, %s:%s}",
                                    "\"min\"", firstMin,
                                    "\"max\"", firstMax,
                                    "\"percentiles\"", "\"" + firstPercentiles.toString() +  "\"" ),
                            "\"completion\"", String.format("{%s:%s, %s:%s, %s:%s}",
                                    "\"min\"", completionMin,
                                    "\"max\"", completionMax,
                                    "\"percentiles\"", "\"" + completionPercentiles.toString() + "\"")
                    );
                    System.out.println();
                });
            }
        }
    }
}
