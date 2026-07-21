package tech.provokedynamic.gymcrmworkload.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import tech.provokedynamic.gymcrmworkload.document.MonthSummary;
import tech.provokedynamic.gymcrmworkload.document.TrainerWorkloadDocument;
import tech.provokedynamic.gymcrmworkload.document.YearSummary;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ExtendWith(SpringExtension.class)
@DataMongoTest
class TrainerWorkloadRepositoryTest {

    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:latest");

    static {
        MONGO.start();
    }

    @Autowired
    private TrainerWorkloadRepository repository;

    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
    }

    @BeforeEach
    void cleanUp() {
        repository.deleteAll();
    }

    private TrainerWorkloadDocument sample() {
        var doc = new TrainerWorkloadDocument("jane.smith", "Jane", "Smith", true);
        doc.setYears(List.of(new YearSummary(2025, List.of(new MonthSummary(6, 60)))));
        return doc;
    }

    @Test
    void save_thenFindByUsername_returnsDocument() {
        repository.save(sample());

        var found = repository.findByTrainerUsername("jane.smith");

        assertThat(found).isPresent();
        assertThat(found.get().getYears().getFirst().getMonths().getFirst().getTrainingsSummaryDuration())
                .isEqualTo(60);
    }

    @Test
    void findByTrainerUsername_returnsEmpty_whenNotExists() {
        assertThat(repository.findByTrainerUsername("ghost")).isEmpty();
    }

    @Test
    void existsByTrainerUsername_reflectsPersistedState() {
        repository.save(sample());

        assertThat(repository.existsByTrainerUsername("jane.smith")).isTrue();
        assertThat(repository.existsByTrainerUsername("ghost")).isFalse();
    }

    @Test
    void findByFirstNameAndLastName_usesCompoundIndex() {
        repository.save(sample());

        var results = repository.findByTrainerFirstNameAndTrainerLastName("Jane", "Smith");

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getTrainerUsername()).isEqualTo("jane.smith");
    }

    @Test
    void update_persistsMutatedDocument() {
        var saved = repository.save(sample());
        saved.getYears().getFirst().getMonths().getFirst().setTrainingsSummaryDuration(90);
        repository.save(saved);

        var reloaded = repository.findByTrainerUsername("jane.smith").orElseThrow();

        assertThat(reloaded.getYears().getFirst().getMonths().getFirst().getTrainingsSummaryDuration())
                .isEqualTo(90);
    }
}
