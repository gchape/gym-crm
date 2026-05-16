package tech.provokedynamic.gymcrm.dao;

import jakarta.annotation.Nullable;
import tech.provokedynamic.gymcrm.dto.Profile;
import tech.provokedynamic.gymcrm.dto.Summary;
import tech.provokedynamic.gymcrm.entity.Trainee;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TraineeDao extends UserDao {

    void save(Trainee trainee);

    void update(Trainee trainee);

    void delete(Trainee trainee);

    boolean existsByUsername(String username);

    Optional<Trainee> findByUsername(String username);

    List<Summary.Training> findTrainingsByUsername(
            String username,
            @Nullable LocalDate from,
            @Nullable LocalDate to,
            @Nullable String trainer,
            @Nullable String type
    );

    List<Profile.Trainer> findUnassignedTrainers(String username);

    List<Profile.Trainer> findAssignedTrainers(String username);
}
