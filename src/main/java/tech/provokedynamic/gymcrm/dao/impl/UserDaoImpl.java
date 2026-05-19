package tech.provokedynamic.gymcrm.dao.impl;

import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Primary
@Repository
public final class UserDaoImpl extends AbstractUserDaoImpl {

    public UserDaoImpl(EntityManager em) {
        super(em);
    }
}
