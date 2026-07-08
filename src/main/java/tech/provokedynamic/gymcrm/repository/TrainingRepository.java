package tech.provokedynamic.gymcrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.entity.Training;

import java.util.List;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {

    @Query("select t from Training t join fetch t.trainer where t.trainee.username = :username")
    List<Training> findAllByTraineeUsernameWithTrainer(@Param("username") String username);
}
