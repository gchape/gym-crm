package tech.provokedynamic.gymcrm.service;

import tech.provokedynamic.gymcrm.dto.request.*;

/** Application boundary for changing an account password using its current credentials. */
public interface PasswordService {

    void updatePassword(ChangePasswordRequest request);
}
