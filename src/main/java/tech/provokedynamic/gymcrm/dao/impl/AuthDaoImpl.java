package tech.provokedynamic.gymcrm.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.dao.AuthDao;
import tech.provokedynamic.gymcrm.dao.AuthDao_;

@Repository
public class AuthDaoImpl implements AuthDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public boolean existsByUsernameAndPassword(String username, String password) {
        return AuthDao_.existsByUsernameAndPassword(em, username, password);
    }
}
