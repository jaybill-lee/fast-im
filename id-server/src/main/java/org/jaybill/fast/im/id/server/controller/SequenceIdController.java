package org.jaybill.fast.im.id.server.controller;

import org.apache.commons.lang3.tuple.Pair;
import org.jaybill.fast.im.id.server.service.SequenceIdService;
import org.jaybill.fast.im.net.http.Get;
import org.jaybill.fast.im.net.http.HttpEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@HttpEndpoint(path = "/sequence-id")
public class SequenceIdController {

    @Autowired
    private SequenceIdService sequenceIdService;

    @Get
    public List<Pair<Long, Long>> allocate(String bizId, Integer size) {
        return sequenceIdService.allocate(bizId, size);
    }
}
