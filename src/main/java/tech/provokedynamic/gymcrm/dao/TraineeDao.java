package tech.provokedynamic.gymcrm.dao;

import jakarta.annotation.Nullable;
import org.hibernate.annotations.processing.HQL;
import org.hibernate.annotations.processing.SQL;
import tech.provokedynamic.gymcrm.dto.Response;
import tech.provokedynamic.gymcrm.entity.Trainee;

import java.time.LocalDate;
import java.util.List;

public interface TraineeDao extends UserDao {

    void save(Trainee trainee);

    @SQL("""
            SELECT count(*) > 0
            FROM "user"
            WHERE username = :username
            """)
    boolean existsByUsername(String username);

    @HQL("""
            SELECT t
            FROM Trainee t
            WHERE t.username = :username
            """)
    Trainee findByUsername(String username);

    @HQL("""
            SELECT new tech.provokedynamic.gymcrm.dto.Response.TraineeProfile(
                t.firstName, t.lastName, t.username, t.dateOfBirth, t.address
            )
            FROM Trainee t
            WHERE t.username = :username
            """)
    Response.TraineeProfile findProfileByUsername(String username);

    void delete(Trainee trainee);

    @HQL("""
            SELECT new tech.provokedynamic.gymcrm.dto.Response.TrainingProfile(
                trn.trainingName,
                trn.trainingDate,
                trn.trainingDuration,
                trn.trainer.username
            )
            FROM Training trn
            WHERE trn.trainee.username = :username
              AND (:from    IS NULL OR trn.trainingDate >= :from)
              AND (:to      IS NULL OR trn.trainingDate <= :to)
              AND (:trainer IS NULL OR trn.trainer.username = :trainer)
              AND (:type    IS NULL OR trn.trainingType.trainingTypeName = :type)
            ORDER BY trn.trainingDate DESC
            """)
    List<Response.TrainingProfile> findTrainingsByUsername(
            String username,
            @Nullable LocalDate from,
            @Nullable LocalDate to,
            @Nullable String trainer,
            @Nullable String type
    );

    @HQL("""
            SELECT new tech.provokedynamic.gymcrm.dto.Response.TrainerProfile(
                tr.firstName,
                tr.lastName,
                tr.username,
                tr.specialization.trainingTypeName
            )
            FROM Trainer tr
            WHERE tr NOT IN (
                SELECT assignedTr
                FROM Trainee t
                JOIN t.trainers assignedTr
                WHERE t.username = :username
            )
            """)
    List<Response.TrainerProfile> findUnassignedTrainers(String username);

    @HQL("""
            SELECT new tech.provokedynamic.gymcrm.dto.Response.TrainerProfile(
                tr.firstName,
                tr.lastName,
                tr.username,
                tr.specialization.trainingTypeName
            )
            FROM Trainee t
            JOIN t.trainers tr
            WHERE t.username = :username
            """)
    List<Response.TrainerProfile> findAssignedTrainers(String username);
}
