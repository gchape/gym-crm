package tech.provokedynamic.gymcrm.dao;

import jakarta.annotation.Nullable;
import tech.provokedynamic.gymcrm.dto.Summary;
import tech.provokedynamic.gymcrm.entity.Trainer;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainerDao extends UserDao {

    void save(Trainer trainer);

    void update(Trainer trainer);

    boolean existsByUsername(String username);

    Optional<Trainer> findByUsername(String username);

    List<Trainer> findByUsernames(List<String> usernames);

    List<Summary.Training> findTrainingsByUsername(
            String username,
            @Nullable LocalDate from,
            @Nullable LocalDate to,
            @Nullable String trainee
    );
}
