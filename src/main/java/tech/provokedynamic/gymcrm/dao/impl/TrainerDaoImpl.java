package tech.provokedynamic.gymcrm.dao.impl;

import jakarta.annotation.Nullable;
import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.dao.TrainerDao;
import tech.provokedynamic.gymcrm.dao.TrainerDao_;
import tech.provokedynamic.gymcrm.dto.Response;
import tech.provokedynamic.gymcrm.entity.Trainer;

import java.time.LocalDate;
import java.util.List;

@Repository
public class TrainerDaoImpl extends UserDaoImpl implements TrainerDao {

    public void save(Trainer trainer) {
        em.persist(trainer);
    }

    @Override
    public boolean existsByUsername(String username) {
        return TrainerDao_.existsByUsername(em, username);
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
    public List<Response.TrainingProfile> findTrainingsByUsername(
            String username,
            @Nullable LocalDate from,
            @Nullable LocalDate to,
            @Nullable String trainee
    ) {
        return TrainerDao_.findTrainingsByUsername(em, username, from, to, trainee);
    }
}
