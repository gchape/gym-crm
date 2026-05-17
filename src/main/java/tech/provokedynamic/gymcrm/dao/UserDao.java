package tech.provokedynamic.gymcrm.dao;

import tech.provokedynamic.gymcrm.entity.User;

public interface UserDao {

    void save(User user);

    void update(User user);

    boolean existsByUsernameIncludingDeleted(String username);

    boolean existsByUsernameAndPassword(String username, String password);

    void updatePassword(String username, String newPassword);

    int deactivateByUsername(String username);

    int activateByUsername(String username);
}
