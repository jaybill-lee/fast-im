package org.jaybill.fast.im.user.center.service;

import org.jaybill.fast.im.user.center.dto.UserSimpleDTO;
import org.jaybill.fast.im.user.center.dto.req.RegisterReq;

public interface RegisterService {

    UserSimpleDTO register(RegisterReq req);
}
