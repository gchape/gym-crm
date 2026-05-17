package tech.provokedynamic.gymcrm.dao.impl;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public final class UserDaoImpl extends AbstractUserDaoImpl {

    public UserDaoImpl(EntityManager em) {
        super(em);
    }
}
