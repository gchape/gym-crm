package tech.provokedynamic.gymcrm.dao.impl;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.dao.TrainingDao;
import tech.provokedynamic.gymcrm.entity.Training;

@Repository
public final class TrainingDaoImpl implements TrainingDao {

    private final EntityManager em;

    public TrainingDaoImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(Training training) {
        em.persist(training);
    }
}
