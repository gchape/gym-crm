package tech.provokedynamic.gymcrm.dao;

import jakarta.annotation.Nullable;
import org.hibernate.annotations.processing.HQL;
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

    @HQL("""
            SELECT count(u.id) > 0
            FROM User u
            WHERE u.username = :username
            """)
    boolean existsByUsername(String username);

    Optional<Trainee> findByUsername(String username);

    @HQL("""
            SELECT new tech.provokedynamic.gymcrm.dto.Summary.Training(
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
    List<Summary.Training> findTrainingsByUsername(
            String username,
            @Nullable LocalDate from,
            @Nullable LocalDate to,
            @Nullable String trainer,
            @Nullable String type
    );

    @HQL("""
            SELECT new tech.provokedynamic.gymcrm.dto.Profile.Trainer(
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
    List<Profile.Trainer> findUnassignedTrainers(String username);

    @HQL("""
            SELECT new tech.provokedynamic.gymcrm.dto.Profile.Trainer(
                tr.firstName,
                tr.lastName,
                tr.username,
                tr.specialization.trainingTypeName
            )
            FROM Trainee t
            JOIN t.trainers tr
            WHERE t.username = :username
            """)
    List<Profile.Trainer> findAssignedTrainers(String username);
}
