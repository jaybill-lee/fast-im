package org.jaybill.fast.im.id.server.dao;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.jaybill.fast.im.id.server.model.SequenceId;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class SequenceIdDao {

    /**
     * Insert a record, sequenceId.bizId is unique id.
     * @param sequenceId model
     * @param connection db connection
     */
    public void insert(SequenceId sequenceId, Connection connection) {
        try {
            var statement = connection.prepareStatement(
                    "insert into sequence_id(app, biz_id, id, start_id, distance, increment) values (?, ?, ?, ?, ?, ?)");
            statement.setString(1, sequenceId.getApp());
            statement.setString(2, sequenceId.getBizId());
            statement.setLong(3, sequenceId.getId());
            statement.setLong(4, sequenceId.getStartId());
            statement.setLong(5, sequenceId.getDistance());
            statement.setLong(6, sequenceId.getIncrement());
            statement.execute();
        } catch (SQLException e) {
            log.error("insert error, model:{}, e:", sequenceId, e);
        }
    }

    /**
     * update id
     * @param app app
     * @param bizId unique id
     * @param size size
     * @param connection db connection
     * @return id range, including the head but not the tail
     */
    public List<Pair<Long, Long>> update(String app, String bizId, long size, Connection connection) {
        var resultList = new ArrayList<Pair<Long, Long>>();
        try {
            connection.setAutoCommit(false);
            // 1. select for update
            var selectStmt = connection.prepareStatement(
                    "select * from sequence_id where biz_id = ? and app = ? for update");
            selectStmt.setString(1, bizId);
            selectStmt.setString(2, app);
            var resultSet = selectStmt.executeQuery();
            if (resultSet.next()) {
                var id = resultSet.getLong("id");
                var startId = resultSet.getLong("start_id");
                var distance = resultSet.getLong("distance");
                var increment = resultSet.getLong("increment");

                // 2. update
                long remaining = startId + distance - id;
                if (remaining > size) {
                    var updateStmt = connection.prepareStatement(
                            "update sequence_id set id = ? where biz_id = ?");
                    updateStmt.setLong(1, id + size);
                    updateStmt.setString(2, bizId);
                    updateStmt.executeUpdate();
                    resultList.add(Pair.of(id, id + size - 1));
                } else if (remaining == size) {
                    var updateStmt = connection.prepareStatement(
                            "update sequence_id set id = ?, start_id = ? where biz_id = ?");
                    var newStartId = startId + increment;
                    updateStmt.setLong(1, newStartId);
                    updateStmt.setLong(2, newStartId);
                    updateStmt.setString(3, bizId);
                    updateStmt.executeUpdate();
                    resultList.add(Pair.of(id, startId + distance - 1));
                } else {
                    resultList.add(Pair.of(id, startId + distance - 1));
                    var loopCnt = (size - remaining) / distance;
                    var newStartId = startId;
                    for (int i = 0; i < loopCnt; i++) {
                        newStartId += increment;
                        resultList.add(Pair.of(newStartId, newStartId + distance - 1));
                    }
                    long remainingSize = (size - remaining) % distance;
                    newStartId += increment;
                    var updateStmt = connection.prepareStatement(
                            "update sequence_id set id = ?, start_id = ? where biz_id = ?");
                    updateStmt.setLong(1, newStartId + remainingSize);
                    updateStmt.setLong(2, newStartId);
                    updateStmt.setString(3, bizId);
                    updateStmt.executeUpdate();
                    if (remainingSize != 0) {
                        resultList.add(Pair.of(newStartId, newStartId + remainingSize - 1));
                    }
                }
            }
            connection.commit();
            return resultList;
        } catch (SQLException e) {
            log.error("update id error:", e);
            try {
                connection.rollback();
            } catch (SQLException ex) {
                log.error("update id rollback error:", e);
            }
            return resultList;
        }
    }
}
