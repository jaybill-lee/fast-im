package org.jaybill.fast.im.connector.service;

import org.jaybill.fast.im.connector.service.dto.UserDTO;

public interface UserService {
    UserDTO getById(String id);
}
