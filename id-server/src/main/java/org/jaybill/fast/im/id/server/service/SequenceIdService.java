package org.jaybill.fast.im.id.server.service;

import org.apache.commons.lang3.tuple.Pair;
import org.jaybill.fast.im.id.server.model.SequenceId;

import java.util.List;

public interface SequenceIdService {

    void init(SequenceId sequenceId);

    List<Pair<Long, Long>> allocate(String bizId, int size);
}
