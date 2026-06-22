package tech.provokedynamic.gymcrm.service;

import tech.provokedynamic.gymcrm.dto.Request;

public interface UserService {

    void updatePassword(Request.ChangePassword request);
}
