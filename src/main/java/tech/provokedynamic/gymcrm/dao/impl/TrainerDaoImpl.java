package tech.provokedynamic.gymcrm.dao.impl;

import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.hibernate.jpa.AvailableHints;
import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.dao.TrainerDao;
import tech.provokedynamic.gymcrm.dto.Summary;
import tech.provokedynamic.gymcrm.entity.Trainer;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class TrainerDaoImpl extends UserDaoImpl implements TrainerDao {

    public TrainerDaoImpl(EntityManager em) {
        super(em);
    }

    @Override
    public void save(Trainer trainer) {
        em.persist(trainer);
    }

    @Override
    public void update(Trainer trainer) {
        em.merge(trainer);
    }

    @Override
    public boolean existsByUsername(String username) {
        return em.createQuery(
                        "SELECT count(tr.id) > 0 FROM Trainer tr WHERE tr.username = :username",
                        Boolean.class)
                .setParameter("username", username)
                .setHint(AvailableHints.HINT_READ_ONLY, true)
                .getSingleResult();
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        try {
            return Optional.of(
                    em.createQuery(
                                    "FROM Trainer tr WHERE tr.username = :username",
                                    Trainer.class)
                            .setParameter("username", username)
                            .getSingleResult()
            );
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Trainer> findByUsernames(List<String> usernames) {
        return em.createQuery(
                        "FROM Trainer tr WHERE tr.username IN :usernames",
                        Trainer.class)
                .setParameter("usernames", usernames)
                .getResultList();
    }

    @Override
    public List<Summary.Training> findTrainingsByUsername(
            String username,
            @Nullable LocalDate from,
            @Nullable LocalDate to,
            @Nullable String trainee
    ) {
        return em.createQuery("""
                        SELECT new tech.provokedynamic.gymcrm.dto.Summary.Training(
                            t.trainingName,
                            t.trainingDate,
                            t.trainingDuration,
                            t.trainee.username
                        )
                        FROM Training t
                        WHERE t.trainer.username = :username
                          AND (:from    IS NULL OR t.trainingDate >= :from)
                          AND (:to      IS NULL OR t.trainingDate <= :to)
                          AND (:trainee IS NULL OR t.trainee.username = :trainee)
                        ORDER BY t.trainingDate DESC
                        """, Summary.Training.class)
                .setParameter("username", username)
                .setParameter("from", from)
                .setParameter("to", to)
                .setParameter("trainee", trainee)
                .getResultList();
    }
}
