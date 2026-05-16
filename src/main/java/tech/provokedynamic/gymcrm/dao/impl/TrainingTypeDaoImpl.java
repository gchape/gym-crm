package tech.provokedynamic.gymcrm.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.dao.TrainingTypeDao;
import tech.provokedynamic.gymcrm.entity.TrainingType;
import tech.provokedynamic.gymcrm.exception.TrainingTypeNotFoundException;

@Repository
public class TrainingTypeDaoImpl implements TrainingTypeDao {

    private final EntityManager em;

    public TrainingTypeDaoImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public TrainingType findByName(String name) {
        try {
            return em.createQuery(
                            "FROM TrainingType tt WHERE tt.trainingTypeName = :name",
                            TrainingType.class)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new TrainingTypeNotFoundException(name);
        }
    }
}
