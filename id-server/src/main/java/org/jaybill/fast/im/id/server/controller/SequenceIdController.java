package org.jaybill.fast.im.id.server.controller;

import org.apache.commons.lang3.tuple.Pair;
import org.jaybill.fast.im.id.server.controller.filter.AuthFilter;
import org.jaybill.fast.im.id.server.model.SequenceId;
import org.jaybill.fast.im.id.server.service.SequenceIdService;
import org.jaybill.fast.im.net.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@HttpEndpoint(path = "/sequence-id")
public class SequenceIdController {

    @Autowired
    private SequenceIdService sequenceIdService;

    @Post
    public boolean init(@JsonBody SequenceId sequenceId, HttpContext ctx) {
        sequenceId.setApp(AuthFilter.getApp(ctx)); // set app
        sequenceIdService.init(sequenceId);
        return true;
    }

    @Put
    public List<Pair<Long, Long>> allocate(String bizId, Integer size, HttpContext ctx) {
        return sequenceIdService.allocate(AuthFilter.getApp(ctx), bizId, size);
    }
}
