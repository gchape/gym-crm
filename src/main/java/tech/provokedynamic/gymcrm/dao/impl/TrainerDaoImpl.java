package tech.provokedynamic.gymcrm.dao.impl;

import jakarta.annotation.Nullable;
import jakarta.persistence.NoResultException;
import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.dao.TrainerDao;
import tech.provokedynamic.gymcrm.dao.TrainerDao_;
import tech.provokedynamic.gymcrm.dto.Summary;
import tech.provokedynamic.gymcrm.entity.Trainer;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class TrainerDaoImpl extends UserDaoImpl implements TrainerDao {

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
        return TrainerDao_.existsByUsername(em, username);
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        try {
            return Optional.of(
                    em.createQuery("SELECT tr FROM Trainer tr WHERE tr.username = :username", Trainer.class)
                            .setParameter("username", username)
                            .getSingleResult()
            );
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Trainer> findByUsernames(List<String> usernames) {
        return TrainerDao_.findByUsernames(em, usernames);
    }

    @Override
    public List<Summary.Training> findTrainingsByUsername(
            String username,
            @Nullable LocalDate from,
            @Nullable LocalDate to,
            @Nullable String trainee
    ) {
        return TrainerDao_.findTrainingsByUsername(em, username, from, to, trainee);
    }
}
