package tech.provokedynamic.gymcrm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.provokedynamic.gymcrm.aspect.ValidationAspect;
import tech.provokedynamic.gymcrm.component.CredentialGenerator;
import tech.provokedynamic.gymcrm.component.InMemoryStorage;
import tech.provokedynamic.gymcrm.component.StorageKeyFormatter;
import tech.provokedynamic.gymcrm.dao.TrainerDao;
import tech.provokedynamic.gymcrm.dto.TrainerRequest;
import tech.provokedynamic.gymcrm.dto.TrainerResponse;
import tech.provokedynamic.gymcrm.entity.Entity;
import tech.provokedynamic.gymcrm.model.Specialization;
import tech.provokedynamic.gymcrm.service.impl.TrainerServiceImpl;
import tech.provokedynamic.gymcrm.storage.Storage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        TrainerServiceImpl.class,
        TrainerServiceImplTest.TestConfig.class,
        TrainerDao.class,
        CredentialGenerator.class,
        ValidationAspect.class,
        InMemoryStorage.class,
        StorageKeyFormatter.class
})
@TestPropertySource(locations = "classpath:application.properties")
@EnableAspectJAutoProxy
class TrainerServiceImplTest {

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private Storage<Entity> storage;

    @BeforeEach
    void setUp() {
        storage.clear();
    }

    private TrainerRequest.Create validCreateRequest() {
        return new TrainerRequest.Create("John", "Doe", Specialization.FITNESS);
    }

    @Test
    void shouldCreateTrainer_whenRequestIsValid() {
        TrainerResponse.Detail result = trainerService.create(validCreateRequest());

        assertThat(result).isNotNull();
        assertThat(result.username()).isNotBlank();
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void shouldThrowException_whenFirstNameIsBlank() {
        TrainerRequest.Create request = new TrainerRequest.Create("", "Doe", Specialization.FITNESS);

        assertThatThrownBy(() -> trainerService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenLastNameIsBlank() {
        TrainerRequest.Create request = new TrainerRequest.Create("John", "", Specialization.FITNESS);

        assertThatThrownBy(() -> trainerService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenSpecializationIsNull() {
        TrainerRequest.Create request = new TrainerRequest.Create("John", "Doe", null);

        assertThatThrownBy(() -> trainerService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldGenerateUniqueUsernames_whenTwoTrainersHaveSameName() {
        TrainerResponse.Detail first = trainerService.create(validCreateRequest());
        TrainerResponse.Detail second = trainerService.create(validCreateRequest());

        assertThat(first.username()).isNotEqualTo(second.username());
    }

    @Test
    void shouldUpdateTrainer_whenRequestIsValid() {
        TrainerResponse.Detail created = trainerService.create(validCreateRequest());

        TrainerRequest.Update update = new TrainerRequest.Update("Johnny", "Doe", Specialization.YOGA, true);

        TrainerResponse.Detail updated = trainerService.update(created.id(), update);

        assertThat(updated.firstName()).isEqualTo("Johnny");
        assertThat(updated.specialization()).isEqualTo(Specialization.YOGA);
    }

    @Test
    void shouldPreserveUsername_whenUpdatingTrainer() {
        TrainerResponse.Detail created = trainerService.create(validCreateRequest());

        TrainerRequest.Update update = new TrainerRequest.Update("Johnny", "Doe", Specialization.YOGA, true);

        TrainerResponse.Detail updated = trainerService.update(created.id(), update);

        assertThat(updated.username()).isEqualTo(created.username());
    }

    @Test
    void shouldThrowException_whenUpdatingWithBlankFirstName() {
        TrainerResponse.Detail created = trainerService.create(validCreateRequest());

        TrainerRequest.Update update = new TrainerRequest.Update("", "Doe", Specialization.FITNESS, true);

        assertThatThrownBy(() -> trainerService.update(created.id(), update))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenUpdatingWithNullSpecialization() {
        TrainerResponse.Detail created = trainerService.create(validCreateRequest());

        TrainerRequest.Update update = new TrainerRequest.Update("John", "Doe", null, true);

        assertThatThrownBy(() -> trainerService.update(created.id(), update))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenUpdatingNonExistentTrainer() {
        TrainerRequest.Update update = new TrainerRequest.Update("John", "Doe", Specialization.FITNESS, true);

        assertThatThrownBy(() -> trainerService.update(999999L, update))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldFindTrainerById_whenTrainerExists() {
        TrainerResponse.Detail created = trainerService.create(validCreateRequest());

        TrainerResponse.Detail found = trainerService.findById(created.id());

        assertThat(found).isEqualTo(created);
    }

    @Test
    void shouldThrowException_whenFindByIdAndTrainerDoesNotExist() {
        assertThatThrownBy(() -> trainerService.findById(999999L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldReturnAllTrainers_whenFindAllCalled() {
        trainerService.create(validCreateRequest());
        trainerService.create(new TrainerRequest.Create("Jane", "Smith", Specialization.YOGA));

        List<TrainerResponse.Summary> result = trainerService.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldDeleteTrainer_whenTrainerExists() {
        TrainerResponse.Detail created = trainerService.create(validCreateRequest());

        trainerService.delete(created.id());

        assertThatThrownBy(() -> trainerService.findById(created.id()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {
    }
}
