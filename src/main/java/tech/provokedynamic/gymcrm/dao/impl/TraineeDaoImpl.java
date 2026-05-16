package tech.provokedynamic.gymcrm.dao.impl;

import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.hibernate.jpa.AvailableHints;
import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.dao.TraineeDao;
import tech.provokedynamic.gymcrm.dto.Profile;
import tech.provokedynamic.gymcrm.dto.Summary;
import tech.provokedynamic.gymcrm.entity.Trainee;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class TraineeDaoImpl extends UserDaoImpl implements TraineeDao {

    public TraineeDaoImpl(EntityManager em) {
        super(em);
    }

    @Override
    public void save(Trainee trainee) {
        em.persist(trainee);
    }

    @Override
    public void update(Trainee trainee) {
        em.merge(trainee);
    }

    @Override
    public boolean existsByUsername(String username) {
        return em.createQuery(
                        "SELECT count(t.id) > 0 FROM Trainee t WHERE t.username = :username",
                        Boolean.class)
                .setParameter("username", username)
                .setHint(AvailableHints.HINT_READ_ONLY, true)
                .getSingleResult();
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        try {
            return Optional.of(
                    em.createQuery(
                                    "SELECT t FROM Trainee t WHERE t.username = :username",
                                    Trainee.class)
                            .setParameter("username", username)
                            .getSingleResult()
            );
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public void delete(Trainee trainee) {
        em.remove(em.contains(trainee) ? trainee : em.merge(trainee));
    }

    @Override
    public List<Summary.Training> findTrainingsByUsername(
            String username,
            @Nullable LocalDate from,
            @Nullable LocalDate to,
            @Nullable String trainer,
            @Nullable String type
    ) {
        return em.createQuery("""
                        SELECT new tech.provokedynamic.gymcrm.dto.Summary.Training(
                            t.trainingName,
                            t.trainingDate,
                            t.trainingDuration,
                            t.trainer.username
                        )
                        FROM Training t
                        WHERE t.trainee.username = :username
                          AND (:from    IS NULL OR t.trainingDate >= :from)
                          AND (:to      IS NULL OR t.trainingDate <= :to)
                          AND (:trainer IS NULL OR t.trainer.username = :trainer)
                          AND (:type    IS NULL OR t.trainingType.trainingTypeName = :type)
                        ORDER BY t.trainingDate DESC
                        """, Summary.Training.class)
                .setParameter("username", username)
                .setParameter("from", from)
                .setParameter("to", to)
                .setParameter("trainer", trainer)
                .setParameter("type", type)
                .getResultList();
    }

    @Override
    public List<Profile.Trainer> findUnassignedTrainers(String username) {
        return em.createQuery("""
                        SELECT new tech.provokedynamic.gymcrm.dto.Profile.Trainer(
                            tr.firstName,
                            tr.lastName,
                            tr.username,
                            tr.specialization.trainingTypeName
                        )
                        FROM Trainer tr
                        WHERE NOT EXISTS (
                            SELECT 1
                            FROM Trainee t
                            JOIN t.trainers assignedTr
                            WHERE t.username = :username
                              AND assignedTr = tr
                        )
                        """, Profile.Trainer.class)
                .setParameter("username", username)
                .getResultList();
    }

    @Override
    public List<Profile.Trainer> findAssignedTrainers(String username) {
        return em.createQuery("""
                        SELECT new tech.provokedynamic.gymcrm.dto.Profile.Trainer(
                            tr.firstName,
                            tr.lastName,
                            tr.username,
                            tr.specialization.trainingTypeName
                        )
                        FROM Trainee t
                        JOIN t.trainers tr
                        WHERE t.username = :username
                        """, Profile.Trainer.class)
                .setParameter("username", username)
                .getResultList();
    }
}
