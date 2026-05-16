package tech.provokedynamic.gymcrm.dao;

public interface UserDao {

    boolean existsByUsernameIncludingDeleted(String username);

    boolean existsByUsernameAndPassword(String username, String password);

    void updatePassword(String username, String newPassword);

    int deactivateByUsername(String username);

    int activateByUsername(String username);
}
