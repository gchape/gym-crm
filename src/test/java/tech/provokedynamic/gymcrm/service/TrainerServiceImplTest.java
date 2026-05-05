package tech.provokedynamic.gymcrm.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import tech.provokedynamic.gymcrm.aspect.ValidationAspect;
import tech.provokedynamic.gymcrm.component.CredentialGenerator;
import tech.provokedynamic.gymcrm.component.InMemoryStorage;
import tech.provokedynamic.gymcrm.dao.TrainerDao;
import tech.provokedynamic.gymcrm.dto.TrainerRequest;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.model.Specialization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = {
        TrainerServiceImpl.class,
        TrainerDao.class,
        CredentialGenerator.class,
        ValidationAspect.class,
        InMemoryStorage.class
}, webEnvironment = SpringBootTest.WebEnvironment.NONE,
        useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@EnableAspectJAutoProxy
class TrainerServiceImplTest {

    @Autowired
    private TrainerService trainerService;

    private TrainerRequest.Create validCreateRequest() {
        return new TrainerRequest.Create("John", "Doe", Specialization.FITNESS);
    }

    @Test
    void shouldCreateTrainer_whenRequestIsValid() {
        Trainer result = trainerService.create(validCreateRequest());

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isNotBlank();
        assertThat(result.getPassword()).hasSize(10);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void shouldThrowException_whenFirstNameIsBlank() {
        TrainerRequest.Create request =
                new TrainerRequest.Create("", "Doe", Specialization.FITNESS);

        assertThatThrownBy(() -> trainerService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenLastNameIsBlank() {
        TrainerRequest.Create request =
                new TrainerRequest.Create("John", "", Specialization.FITNESS);

        assertThatThrownBy(() -> trainerService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenSpecializationIsNull() {
        TrainerRequest.Create request =
                new TrainerRequest.Create("John", "Doe", null);

        assertThatThrownBy(() -> trainerService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldGenerateUniqueUsernames_whenTwoTrainersHaveSameName() {
        Trainer first = trainerService.create(validCreateRequest());
        Trainer second = trainerService.create(validCreateRequest());

        assertThat(first.getUsername()).isNotEqualTo(second.getUsername());
    }

    @Test
    void shouldUpdateTrainer_whenRequestIsValid() {
        Trainer created = trainerService.create(validCreateRequest());

        TrainerRequest.Update update =
                new TrainerRequest.Update("Johnny", "Doe", Specialization.YOGA, true);

        Trainer updated = trainerService.update(created.getId(), update);

        assertThat(updated.getFirstName()).isEqualTo("Johnny");
        assertThat(updated.getSpecialization()).isEqualTo(Specialization.YOGA);
    }

    @Test
    void shouldPreserveUsernameAndPassword_whenUpdatingTrainer() {
        Trainer created = trainerService.create(validCreateRequest());

        TrainerRequest.Update update =
                new TrainerRequest.Update("Johnny", "Doe", Specialization.YOGA, true);

        Trainer updated = trainerService.update(created.getId(), update);

        assertThat(updated.getUsername()).isEqualTo(created.getUsername());
        assertThat(updated.getPassword()).isEqualTo(created.getPassword());
    }

    @Test
    void shouldThrowException_whenUpdatingWithBlankFirstName() {
        Trainer created = trainerService.create(validCreateRequest());

        TrainerRequest.Update update =
                new TrainerRequest.Update("", "Doe", Specialization.FITNESS, true);

        assertThatThrownBy(() -> trainerService.update(created.getId(), update))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenUpdatingWithNullSpecialization() {
        Trainer created = trainerService.create(validCreateRequest());

        TrainerRequest.Update update =
                new TrainerRequest.Update("John", "Doe", null, true);

        assertThatThrownBy(() -> trainerService.update(created.getId(), update))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenUpdatingNonExistentTrainer() {
        TrainerRequest.Update update =
                new TrainerRequest.Update("John", "Doe", Specialization.FITNESS, true);

        assertThatThrownBy(() -> trainerService.update(999999L, update))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
