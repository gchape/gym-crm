package tech.provokedynamic.gymcrm.dao.impl;

import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.dao.TraineeDao;
import tech.provokedynamic.gymcrm.dao.TraineeDao_;
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
        return TraineeDao_.existsByUsername(em, username);
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        try {
            return Optional.of(
                    em.createQuery("SELECT t FROM Trainee t WHERE t.username = :username", Trainee.class)
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
        return TraineeDao_.findTrainingsByUsername(em, username, from, to, trainer, type);
    }

    @Override
    public List<Profile.Trainer> findUnassignedTrainers(String username) {
        return TraineeDao_.findUnassignedTrainers(em, username);
    }

    @Override
    public List<Profile.Trainer> findAssignedTrainers(String username) {
        return TraineeDao_.findAssignedTrainers(em, username);
    }
}
