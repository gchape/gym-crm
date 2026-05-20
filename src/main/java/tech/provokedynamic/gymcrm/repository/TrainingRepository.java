package tech.provokedynamic.gymcrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.entity.Training;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {
}
