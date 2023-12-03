package org.jaybill.fast.im.connector.service;

import java.util.Set;

public interface UserRelationService {

    Set<String> getAllFriendIds(String userId);
}
