package tech.provokedynamic.gymcrm.dao.impl;

import jakarta.persistence.EntityManager;
import org.hibernate.jpa.AvailableHints;
import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.dao.UserDao;
import tech.provokedynamic.gymcrm.entity.User;

@Repository
public class UserDaoImpl implements UserDao {

    protected final EntityManager em;

    public UserDaoImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public boolean existsByUsernameIncludingDeleted(String username) {
        return (boolean) em.createNativeQuery(
                        "SELECT count(*) > 0 FROM \"user\" WHERE username = :username")
                .setParameter("username", username)
                .setHint(AvailableHints.HINT_READ_ONLY, true)
                .getSingleResult();
    }

    @Override
    public boolean existsByUsernameAndPassword(String username, String password) {
        return em.createQuery(
                        "SELECT count(u.id) > 0 FROM User u WHERE u.username = :username AND u.password = :password",
                        Boolean.class)
                .setParameter("username", username)
                .setParameter("password", password)
                .setHint(AvailableHints.HINT_READ_ONLY, true)
                .getSingleResult();
    }

    @Override
    public void updatePassword(String username, String newPassword) {
        em.createQuery(
                        "UPDATE User u SET u.password = :newPassword WHERE u.username = :username")
                .setParameter("newPassword", newPassword)
                .setParameter("username", username)
                .executeUpdate();
    }

    @Override
    public int deactivateByUsername(String username) {
        return em.createNativeQuery(
                        "UPDATE \"user\" SET is_active = false WHERE username = :username AND is_active = true")
                .setParameter("username", username)
                .setHint(AvailableHints.HINT_NATIVE_SPACES, User.class)
                .executeUpdate();
    }

    @Override
    public int activateByUsername(String username) {
        return em.createNativeQuery(
                        "UPDATE \"user\" SET is_active = true WHERE username = :username AND is_active = false")
                .setParameter("username", username)
                .setHint(AvailableHints.HINT_NATIVE_SPACES, User.class)
                .executeUpdate();
    }
}
