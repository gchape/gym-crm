package tech.provokedynamic.gymcrm.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.entity.Trainee;

import java.util.Optional;

@Repository
public interface TraineeRepository extends BaseUserRepository<Trainee>, TraineeRepositoryCustom {

    void deleteByUsername(String username);

    @EntityGraph(attributePaths = {"trainers", "trainers.specialization"})
    Optional<Trainee> findWTrainersByUsername(@Param("username") String username);
}
