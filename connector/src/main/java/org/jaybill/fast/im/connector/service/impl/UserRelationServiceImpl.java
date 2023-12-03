package org.jaybill.fast.im.connector.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jaybill.fast.im.connector.service.UserRelationService;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
public class UserRelationServiceImpl implements UserRelationService {

    @Override
    public Set<String> getAllFriendIds(String userId) {
        return null;
    }
}
