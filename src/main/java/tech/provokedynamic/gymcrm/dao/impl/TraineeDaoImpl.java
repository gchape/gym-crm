package tech.provokedynamic.gymcrm.dao.impl;

import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.dao.TraineeDao;
import tech.provokedynamic.gymcrm.dao.TraineeDao_;
import tech.provokedynamic.gymcrm.dto.Response;
import tech.provokedynamic.gymcrm.entity.Trainee;

import java.time.LocalDate;
import java.util.List;

@Repository
public class TraineeDaoImpl implements TraineeDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void save(Trainee trainee) {
        em.persist(trainee);
    }

    @Override
    public boolean existsByUsername(String username) {
        return TraineeDao_.existsByUsername(em, username);
    }

    @Override
    public boolean existsByUsernameAndPassword(String username, String password) {
        return TraineeDao_.existsByUsernameAndPassword(em, username, password);
    }

    @Override
    public Trainee findByUsername(String username) {
        return TraineeDao_.findByUsername(em, username);
    }

    @Override
    public Response.TraineeProfile findProfileByUsername(String username) {
        return TraineeDao_.findProfileByUsername(em, username);
    }

    @Override
    public void updatePassword(String username, String newPassword) {
        TraineeDao_.updatePassword(em, username, newPassword);
    }

    @Override
    public void deactivateByUsername(String username) {
        TraineeDao_.deactivateByUsername(em, username);
    }

    @Override
    public void activateByUsername(String username) {
        em.createNativeQuery(TraineeDao_.ACTIVATE_BY_USERNAME_String)
                .setParameter("username", username)
                .executeUpdate();
    }

    @Override
    public void delete(Trainee trainee) {
        em.remove(em.contains(trainee) ? trainee : em.merge(trainee));
    }

    @Override
    public List<Response.TrainingProfile> findTrainingsByUsername(
            String username,
            @Nullable LocalDate from,
            @Nullable LocalDate to,
            @Nullable String trainer,
            @Nullable String type
    ) {
        return TraineeDao_.findTrainingsByUsername(em, username, from, to, trainer, type);
    }

    @Override
    public List<Response.TrainerProfile> findUnassignedTrainers(String username) {
        return TraineeDao_.findUnassignedTrainers(em, username);
    }

    @Override
    public List<Response.TrainerProfile> findAssignedTrainers(String username) {
        return TraineeDao_.findAssignedTrainers(em, username);
    }
}
