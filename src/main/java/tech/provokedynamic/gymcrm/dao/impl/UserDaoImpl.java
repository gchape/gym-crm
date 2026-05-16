package tech.provokedynamic.gymcrm.dao.impl;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.dao.UserDao;
import tech.provokedynamic.gymcrm.dao.UserDao_;

@Repository
public class UserDaoImpl implements UserDao {

    protected final EntityManager em;

    public UserDaoImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public boolean existsByUsernameIncludingDeleted(String username) {
        return UserDao_.existsByUsernameIncludingDeleted(em, username);
    }

    @Override
    public boolean existsByUsernameAndPassword(String username, String password) {
        return UserDao_.existsByUsernameAndPassword(em, username, password);
    }

    @Override
    public void updatePassword(String username, String newPassword) {
        UserDao_.updatePassword(em, username, newPassword);
    }

    @Override
    public int deactivateByUsername(String username) {
        return UserDao_.deactivateByUsername(em, username);
    }

    @Override
    public int activateByUsername(String username) {
        return UserDao_.activateByUsername(em, username);
    }
}
