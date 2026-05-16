package tech.provokedynamic.gymcrm.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.dao.TrainingDao;
import tech.provokedynamic.gymcrm.entity.Training;

@Repository
public class TrainingDaoImpl implements TrainingDao {

    @PersistenceContext
    private final EntityManager em;

    public TrainingDaoImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(Training training) {
        em.persist(training);
    }
}
