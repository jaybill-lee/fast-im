package org.jaybill.fast.im.id.server.service.impl;

import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.jaybill.fast.im.common.util.AssertUtil;
import org.jaybill.fast.im.id.server.MariaPoolDataSourceGroup;
import org.jaybill.fast.im.id.server.dao.SequenceIdDao;
import org.jaybill.fast.im.id.server.model.SequenceId;
import org.jaybill.fast.im.id.server.service.SequenceIdService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class SequenceIdServiceImpl implements SequenceIdService {

    @Autowired
    private SequenceIdDao sequenceIdDao;
    @Autowired
    private MariaPoolDataSourceGroup dataSourceGroup;
    @Autowired
    private StatefulRedisClusterConnection<String, String> redisConnection;

    @Override
    public void init(SequenceId sequenceId) {
        AssertUtil.isTrue(sequenceId.getIncrement() >= sequenceId.getDistance() * dataSourceGroup.size());
        AtomicInteger counter = new AtomicInteger(1);
        dataSourceGroup.consume(dataSource -> {
            try (Connection connection = dataSource.getConnection()) {
                var model = new SequenceId();
                BeanUtils.copyProperties(sequenceId, model);

                // init id
                var startId = sequenceId.getStartId() + counter.get() * sequenceId.getDistance();
                model.setId(startId);
                model.setStartId(startId);
                counter.getAndIncrement();

                sequenceIdDao.insert(model, connection);
            } catch (SQLException e) {
                log.error("maria get connection error:", e);
            }
        });
    }

    @Override
    public List<Pair<Long, Long>> allocate(String bizId, int size) {
        var index = redisConnection.sync().incr("dsfbj");
        var dataSource = dataSourceGroup.get(index);
        try (var connection = dataSource.getConnection()) {
            return sequenceIdDao.update(bizId, size, connection);
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }
}
