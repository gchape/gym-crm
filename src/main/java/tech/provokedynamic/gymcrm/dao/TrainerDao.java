package tech.provokedynamic.gymcrm.dao;

import jakarta.annotation.Nullable;
import org.hibernate.annotations.processing.HQL;
import tech.provokedynamic.gymcrm.dto.Summary;
import tech.provokedynamic.gymcrm.entity.Trainer;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainerDao extends UserDao {

    void save(Trainer trainer);

    void update(Trainer trainer);

    @HQL("""
            SELECT count(u.id) > 0
            FROM User u
            WHERE u.username = :username
            """)
    boolean existsByUsername(String username);

    Optional<Trainer> findByUsername(String username);

    @HQL("""
            SELECT tr
            FROM Trainer tr
            WHERE tr.username IN :usernames
            """)
    List<Trainer> findByUsernames(List<String> usernames);

    @HQL("""
            SELECT new tech.provokedynamic.gymcrm.dto.Summary.Training(
                trn.trainingName,
                trn.trainingDate,
                trn.trainingDuration,
                trn.trainee.username
            )
            FROM Training trn
            WHERE trn.trainer.username = :username
              AND (:from    IS NULL OR trn.trainingDate >= :from)
              AND (:to      IS NULL OR trn.trainingDate <= :to)
              AND (:trainee IS NULL OR trn.trainee.username = :trainee)
            ORDER BY trn.trainingDate DESC
            """)
    List<Summary.Training> findTrainingsByUsername(
            String username,
            @Nullable LocalDate from,
            @Nullable LocalDate to,
            @Nullable String trainee
    );
}
