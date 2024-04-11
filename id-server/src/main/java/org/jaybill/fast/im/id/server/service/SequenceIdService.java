package org.jaybill.fast.im.id.server.service;

import org.apache.commons.lang3.tuple.Pair;
import org.jaybill.fast.im.id.server.model.SequenceId;

import java.util.List;

public interface SequenceIdService {

    /**
     * Initialize sequenceId, which inserts a record into the database based on bizId.
     * If the record exists, ignore it. <br/>
     * <b>Required parameters:</b>
     * <li>app</li>
     * <li>bizId</li>
     * <li>startId</li>
     * <li>distance</li>
     * <li>increment</li>
     */
    void init(SequenceId sequenceId);

    /**
     * allocate id
     * @param app app
     * @param bizId bizId
     * @param size size
     * @return
     */
    List<Pair<Long, Long>> allocate(String app, String bizId, int size);
}
