package tech.provokedynamic.gymcrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.entity.Training;

import java.util.List;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long>, JpaSpecificationExecutor<Training> {

    @Override
    @EntityGraph(attributePaths = {"trainee", "trainer", "trainingType"})
    Page<Training> findAll(Specification<Training> specification, Pageable pageable);

    @Query("select t from Training t join fetch t.trainer where t.trainee.username = :username")
    List<Training> findAllByTraineeUsernameWithTrainer(@Param("username") String username);
}
