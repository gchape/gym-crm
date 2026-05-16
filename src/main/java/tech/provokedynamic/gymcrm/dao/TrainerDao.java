package tech.provokedynamic.gymcrm.dao;

import jakarta.annotation.Nullable;
import org.hibernate.annotations.processing.HQL;
import org.hibernate.annotations.processing.SQL;
import tech.provokedynamic.gymcrm.dto.Response;
import tech.provokedynamic.gymcrm.entity.Trainer;

import java.time.LocalDate;
import java.util.List;

public interface TrainerDao extends UserDao {

    void save(Trainer training);

    @SQL("""
            SELECT count(*) > 0
            FROM "user"
            WHERE username = :username
            """)
    boolean existsByUsername(String username);

    @HQL("""
            SELECT tr
            FROM Trainer tr
            WHERE tr.username = :username
            """)
    Trainer findByUsername(String username);

    @HQL("""
            SELECT new tech.provokedynamic.gymcrm.dto.Response.TrainerProfile(
                tr.firstName,
                tr.lastName,
                tr.username,
                tr.specialization.trainingTypeName
            )
            FROM Trainer tr
            WHERE tr.username = :username
            """)
    Response.TrainerProfile findProfileByUsername(String username);

    @HQL("""
            SELECT new tech.provokedynamic.gymcrm.dto.Response.TrainingProfile(
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
    List<Response.TrainingProfile> findTrainingsByUsername(
            String username,
            @Nullable LocalDate from,
            @Nullable LocalDate to,
            @Nullable String trainee
    );
}
