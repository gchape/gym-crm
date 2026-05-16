package tech.provokedynamic.gymcrm.dao.impl;

import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.dao.TrainerDao;
import tech.provokedynamic.gymcrm.dao.TrainerDao_;
import tech.provokedynamic.gymcrm.dto.Response;
import tech.provokedynamic.gymcrm.entity.Trainer;

import java.time.LocalDate;
import java.util.List;

@Repository
public class TrainerDaoImpl implements TrainerDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void save(Trainer trainer) {
        em.persist(trainer);
    }

    @Override
    public boolean existsByUsernameIncludingDeleted(String username) {
        return TrainerDao_.existsByUsernameIncludingDeleted(em, username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return TrainerDao_.existsByUsername(em, username);
    }

    @Override
    public boolean existsByUsernameAndPassword(String username, String password) {
        return TrainerDao_.existsByUsernameAndPassword(em, username, password);
    }

    @Override
    public Trainer findByUsername(String username) {
        return TrainerDao_.findByUsername(em, username);
    }

    @Override
    public Response.TrainerProfile findProfileByUsername(String username) {
        return TrainerDao_.findProfileByUsername(em, username);
    }

    @Override
    public void updatePassword(String username, String newPassword) {
        TrainerDao_.updatePassword(em, username, newPassword);
    }

    @Override
    public void deactivateByUsername(String username) {
        TrainerDao_.deactivateByUsername(em, username);
    }

    @Override
    public void activateByUsername(String username) {
        em.createNativeQuery(TrainerDao_.ACTIVATE_BY_USERNAME_String)
                .setParameter("username", username)
                .executeUpdate();
    }

    @Override
    public List<Response.TrainingProfile> findTrainingsByUsername(
            String username,
            @Nullable LocalDate from,
            @Nullable LocalDate to,
            @Nullable String trainee
    ) {
        return TrainerDao_.findTrainingsByUsername(em, username, from, to, trainee);
    }
}
