package tech.provokedynamic.gymcrm.validation;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tech.provokedynamic.gymcrm.dto.request.*;
import tech.provokedynamic.gymcrm.repository.UserRepository;
import tech.provokedynamic.gymcrm.util.DatabaseCredentialGenerator;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class BeanValidationRequestValidatorTest {

    private static ValidatorFactory validatorFactory;
    private static InputValidator<ValidatedRequest> requestValidator;
    private static DatabaseCredentialGenerator credentialGenerator;

    @BeforeAll
    static void beforeAll() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        requestValidator = new BeanValidationRequestValidator(validatorFactory.getValidator());
        credentialGenerator = new DatabaseCredentialGenerator(mock(UserRepository.class));
    }

    @AfterAll
    static void afterAll() {
        validatorFactory.close();
    }

    @Test
    void validate_doesNotThrow_whenChangePasswordRequestIsValid() {
        var request = new ChangePasswordRequest(
                "John.Doe",
                credentialGenerator.generatePassword(),
                credentialGenerator.generatePassword()
        );

        assertThatCode(() -> requestValidator.validate(request)).doesNotThrowAnyException();
    }

    @Test
    void validate_throws_whenChangePasswordHasBlankUsername() {
        var request = new ChangePasswordRequest(
                "",
                credentialGenerator.generatePassword(),
                credentialGenerator.generatePassword()
        );

        assertThatThrownBy(() -> requestValidator.validate(request))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void validate_throws_whenChangePasswordHasShortNewPassword() {
        var request = new ChangePasswordRequest(
                "John.Doe",
                credentialGenerator.generatePassword(),
                "short"
        );

        assertThatThrownBy(() -> requestValidator.validate(request))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void validate_throws_whenChangePasswordHasLongNewPassword() {
        var request = new ChangePasswordRequest(
                "John.Doe",
                credentialGenerator.generatePassword(),
                "x".repeat(73)
        );

        assertThatThrownBy(() -> requestValidator.validate(request))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void validate_doesNotThrow_whenCreateTraineeRequestIsValid() {
        var request = new RegisterTraineeRequest("John", "Doe", LocalDate.of(1990, 1, 1), null);

        assertThatCode(() -> requestValidator.validate(request)).doesNotThrowAnyException();
    }

    @Test
    void validate_throws_whenCreateTraineeHasBlankFirstName() {
        var request = new RegisterTraineeRequest("", "Doe", null, null);

        assertThatThrownBy(() -> requestValidator.validate(request))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void validate_throws_whenCreateTraineeHasFutureDateOfBirth() {
        var request = new RegisterTraineeRequest("John", "Doe", LocalDate.now().plusDays(1), null);

        assertThatThrownBy(() -> requestValidator.validate(request))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void validate_doesNotThrow_whenCreateTrainerRequestIsValid() {
        var request = new RegisterTrainerRequest("Jane", "Smith", "YOGA");

        assertThatCode(() -> requestValidator.validate(request)).doesNotThrowAnyException();
    }

    @Test
    void validate_throws_whenCreateTrainerHasBlankSpecialization() {
        var request = new RegisterTrainerRequest("Jane", "Smith", "");

        assertThatThrownBy(() -> requestValidator.validate(request))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void validate_doesNotThrow_whenAddTrainingRequestIsValid() {
        var request = new ScheduleTrainingRequest(
                "John.Doe",
                "Jane.Smith",
                "Morning Session",
                LocalDate.now(),
                60
        );

        assertThatCode(() -> requestValidator.validate(request)).doesNotThrowAnyException();
    }

    @Test
    void validate_throws_whenAddTrainingHasPastDate() {
        var request = new ScheduleTrainingRequest(
                "John.Doe", "Jane.Smith", "Morning Session",
                LocalDate.now().minusDays(1), 60
        );

        assertThatThrownBy(() -> requestValidator.validate(request))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void validate_throws_whenAddTrainingDurationExceedsMax() {
        var request = new ScheduleTrainingRequest(
                "John.Doe", "Jane.Smith", "Morning Session", LocalDate.now(), 481
        );

        assertThatThrownBy(() -> requestValidator.validate(request))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void validate_throws_whenAddTrainingDurationIsZero() {
        var request = new ScheduleTrainingRequest(
                "John.Doe", "Jane.Smith", "Morning Session", LocalDate.now(), 0
        );

        assertThatThrownBy(() -> requestValidator.validate(request))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void validate_doesNotThrow_whenUpdateTraineeTrainersRequestIsValid() {
        var request = new ReplaceTrainerAssignmentsRequest("John.Doe", List.of("Jane.Smith"));

        assertThatCode(() -> requestValidator.validate(request)).doesNotThrowAnyException();
    }

    @Test
    void validate_allowsClearingTrainerAssignments() {
        var request = new ReplaceTrainerAssignmentsRequest("John.Doe", List.of());

        assertThatCode(() -> requestValidator.validate(request)).doesNotThrowAnyException();
    }

    @Test
    void validate_throws_whenUpdateTraineeTrainersHasBlankUsername() {
        var request = new ReplaceTrainerAssignmentsRequest("John.Doe", List.of(""));

        assertThatThrownBy(() -> requestValidator.validate(request))
                .isInstanceOf(ConstraintViolationException.class);
    }
}
