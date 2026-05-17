package tech.provokedynamic.gymcrm.dao.impl;

import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.hibernate.jpa.AvailableHints;
import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.dao.TrainerDao;
import tech.provokedynamic.gymcrm.dto.Summary;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.model.UserType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public final class TrainerDaoImpl extends AbstractUserDaoImpl implements TrainerDao {

    private static final String FIND_BY_USERNAME =
            "SELECT tr FROM Trainer tr WHERE tr.username = :username";

    private static final String FIND_BY_USERNAMES =
            "SELECT tr FROM Trainer tr WHERE tr.username IN :usernames";

    private static final String EXISTS_BY_USERNAME =
            "SELECT count(tr.id) > 0 FROM Trainer tr WHERE tr.username = :username";

    public TrainerDaoImpl(EntityManager em) {
        super(em);
    }

    @Override
    public boolean existsByUsername(String username) {
        return em.createQuery(EXISTS_BY_USERNAME, Boolean.class)
                .setParameter("username", username)
                .setHint(AvailableHints.HINT_READ_ONLY, true)
                .getSingleResult();
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        try {
            return Optional.of(
                    em.createQuery(FIND_BY_USERNAME, Trainer.class)
                            .setParameter("username", username)
                            .getSingleResult()
            );
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Trainer> findByUsernames(List<String> usernames) {
        return em.createQuery(FIND_BY_USERNAMES, Trainer.class)
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
        return super.findTrainingsByUsername(
                username,
                UserType.TRAINER,
                from,
                to,
                trainee,
                null
        );
    }
}
