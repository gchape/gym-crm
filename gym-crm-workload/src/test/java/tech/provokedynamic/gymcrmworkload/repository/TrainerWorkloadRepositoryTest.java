package tech.provokedynamic.gymcrmworkload.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import tech.provokedynamic.gymcrmworkload.config.SecurityConfig;
import tech.provokedynamic.gymcrmworkload.document.MonthlyWorkload;
import tech.provokedynamic.gymcrmworkload.document.TrainerWorkloadDocument;
import tech.provokedynamic.gymcrmworkload.document.YearlyWorkload;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataMongoTest(excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
class TrainerWorkloadRepositoryTest {

    @Container
    @ServiceConnection
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:9.0.4");

    static {
        MONGO.start();
    }

    @Autowired
    private TrainerWorkloadRepository repository;

    @BeforeEach
    void cleanUp() {
        repository.deleteAll();
    }

    private TrainerWorkloadDocument sample() {
        var doc = new TrainerWorkloadDocument("jane.smith", "Jane", "Smith", true);
        doc.setYears(List.of(new YearlyWorkload(2025, List.of(new MonthlyWorkload(6, 60)))));
        return doc;
    }

    @Test
    void save_thenFindByUsername_returnsDocument() {
        repository.save(sample());

        var found = repository.findByTrainerUsername("jane.smith");

        assertThat(found).isPresent();
        assertThat(found.get().getYears().getFirst().getMonths().getFirst().getDurationMinutes())
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
        saved.getYears().getFirst().getMonths().getFirst().setDurationMinutes(90);
        repository.save(saved);

        var reloaded = repository.findByTrainerUsername("jane.smith").orElseThrow();

        assertThat(reloaded.getYears().getFirst().getMonths().getFirst().getDurationMinutes())
                .isEqualTo(90);
    }

    @Autowired
    private org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;

    @Test
    void renamedDurationStillReadsAndWritesTheExistingMongoField() {
        var legacyMonth = new org.bson.Document("month", 6).append("trainingsSummaryDuration", 90);
        var legacyYear = new org.bson.Document("year", 2025).append("months", List.of(legacyMonth));
        var legacy = new org.bson.Document("trainerUsername", "legacy.trainer")
                .append("trainerFirstName", "Legacy").append("trainerLastName", "Trainer")
                .append("trainerStatus", true).append("years", List.of(legacyYear))
                .append("_class", TrainerWorkloadDocument.class.getName()).append("version", 0L);
        mongoTemplate.getCollection("trainer_workload").insertOne(legacy);
        var loaded = repository.findByTrainerUsername("legacy.trainer").orElseThrow();
        var month = loaded.getYears().getFirst().getMonths().getFirst();
        assertThat(month.getDurationMinutes()).isEqualTo(90);
        month.applyChange(tech.provokedynamic.gymcrmcommon.event.WorkloadEvent.ActionType.ADD, 30);
        repository.save(loaded);
        var stored = mongoTemplate.getCollection("trainer_workload")
                .find(new org.bson.Document("trainerUsername", "legacy.trainer")).first();
        var storedMonth = stored.getList("years", org.bson.Document.class).getFirst()
                .getList("months", org.bson.Document.class).getFirst();
        assertThat(storedMonth.getInteger("trainingsSummaryDuration")).isEqualTo(120);
        assertThat(storedMonth).doesNotContainKey("durationMinutes");
    }
}
