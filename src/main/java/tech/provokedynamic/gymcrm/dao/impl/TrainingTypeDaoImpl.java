package tech.provokedynamic.gymcrm.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.dao.TrainingTypeDao;
import tech.provokedynamic.gymcrm.dao.TrainingTypeDao_;
import tech.provokedynamic.gymcrm.entity.TrainingType;
import tech.provokedynamic.gymcrm.exception.TrainingTypeNotFoundException;

@Repository
public class TrainingTypeDaoImpl implements TrainingTypeDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public TrainingType findByName(String name) {
        try {
            return TrainingTypeDao_.findByName(em, name);
        } catch (NoResultException e) {
            throw new TrainingTypeNotFoundException(name);
        }
    }
}
