package tech.provokedynamic.gymcrmworkload.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrmworkload.document.TrainerWorkloadDocument;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerWorkloadRepository extends MongoRepository<TrainerWorkloadDocument, String> {

    Optional<TrainerWorkloadDocument> findByTrainerUsername(String trainerUsername);

    boolean existsByTrainerUsername(String trainerUsername);

    List<TrainerWorkloadDocument> findByTrainerFirstNameAndTrainerLastName(
            String trainerFirstName, String trainerLastName);
}
