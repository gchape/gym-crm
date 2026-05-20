package tech.provokedynamic.gymcrm.validation.impl;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.repository.UserRepository;
import tech.provokedynamic.gymcrm.util.RepositoryCredentialGenerator;
import tech.provokedynamic.gymcrm.validation.RequestValidator;
import tech.provokedynamic.gymcrm.validation.Validator;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class RequestValidatorImplTest {

    private static ValidatorFactory validatorFactory;

    private static Validator<Request> requestValidator;

    private static RepositoryCredentialGenerator credentialGenerator;

    @BeforeAll
    static void beforeAll() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        requestValidator = new RequestValidator(validatorFactory.getValidator());

        credentialGenerator = new RepositoryCredentialGenerator(mock(UserRepository.class));
    }

    @AfterAll
    static void afterAll() {
        validatorFactory.close();
    }

    @Test
    void validate_doesNotThrow_whenChangePasswordRequestIsValid() {
        var request = new Request.ChangePassword(
                "John.Doe",
                credentialGenerator.generatePassword(),
                credentialGenerator.generatePassword()
        );

        assertThatCode(() -> requestValidator.validate(request)).doesNotThrowAnyException();
    }

    @Test
    void validate_throws_whenChangePasswordHasBlankUsername() {
        var request = new Request.ChangePassword(
                "",
                credentialGenerator.generatePassword(),
                credentialGenerator.generatePassword()
        );

        assertThatThrownBy(() -> requestValidator.validate(request))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void validate_throws_whenChangePasswordHasShortNewPassword() {
        var request = new Request.ChangePassword(
                "John.Doe",
                credentialGenerator.generatePassword(),
                "short"
        );

        assertThatThrownBy(() -> requestValidator.validate(request))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void validate_throws_whenChangePasswordHasLongNewPassword() {
        var request = new Request.ChangePassword(
                "John.Doe",
                credentialGenerator.generatePassword(),
                "thisIsTooLong"
        );

        assertThatThrownBy(() -> requestValidator.validate(request))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void validate_doesNotThrow_whenCreateTraineeRequestIsValid() {
        var request = new Request.CreateTrainee(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                null
        );

        assertThatCode(() -> requestValidator.validate(request)).doesNotThrowAnyException();
    }

    @Test
    void validate_throws_whenCreateTraineeHasBlankFirstName() {
        var request = new Request.CreateTrainee(
                "",
                "Doe",
                null,
                null
        );

        assertThatThrownBy(() -> requestValidator.validate(request))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void validate_throws_whenCreateTraineeHasFutureDateOfBirth() {
        var request = new Request.CreateTrainee(
                "John",
                "Doe",
                LocalDate.now().plusDays(1),
                null
        );

        assertThatThrownBy(() -> requestValidator.validate(request))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void validate_throws_whenCreateTraineeFirstNameIsTooShort() {
        var request = new Request.CreateTrainee(
                "J",
                "Doe",
                null,
                null
        );

        assertThatThrownBy(() -> requestValidator.validate(request))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void validate_throws_whenCreateTraineeFirstNameContainsDigits() {
        var request = new Request.CreateTrainee(
                "John1",
                "Doe",
                null,
                null
        );

        assertThatThrownBy(() -> requestValidator.validate(request))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void validate_doesNotThrow_whenCreateTrainerRequestIsValid() {
        var request = new Request.CreateTrainer(
                "Jane",
                "Smith",
                "YOGA"
        );

        assertThatCode(() -> requestValidator.validate(request)).doesNotThrowAnyException();
    }

    @Test
    void validate_throws_whenCreateTrainerHasBlankSpecialization() {
        var request = new Request.CreateTrainer(
                "Jane",
                "Smith",
                ""
        );

        assertThatThrownBy(() -> requestValidator.validate(request))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void validate_doesNotThrow_whenAddTrainingRequestIsValid() {
        var request = new Request.AddTraining(
                "John.Doe",
                credentialGenerator.generatePassword(),
                "Jane.Smith",
                "Morning Session",
                "YOGA",
                LocalDate.now(),
                60
        );

        assertThatCode(() -> requestValidator.validate(request)).doesNotThrowAnyException();
    }

    @Test
    void validate_throws_whenAddTrainingHasPastDate() {
        var request = new Request.AddTraining(
                "John.Doe",
                credentialGenerator.generatePassword(),
                "Jane.Smith",
                "Morning Session",
                "YOGA",
                LocalDate.now().minusDays(1),
                60
        );

        assertThatThrownBy(() -> requestValidator.validate(request))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void validate_throws_whenAddTrainingDurationExceedsMax() {
        @SuppressWarnings("DataFlowIssue")
        var request = new Request.AddTraining(
                "John.Doe",
                credentialGenerator.generatePassword(),
                "Jane.Smith",
                "Morning Session",
                "YOGA",
                LocalDate.now(),
                481
        );

        assertThatThrownBy(() -> requestValidator.validate(request))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void validate_throws_whenAddTrainingDurationIsZero() {
        @SuppressWarnings("DataFlowIssue")
        var request = new Request.AddTraining(
                "John.Doe",
                credentialGenerator.generatePassword(),
                "Jane.Smith",
                "Morning Session",
                "YOGA",
                LocalDate.now(),
                0
        );

        assertThatThrownBy(() -> requestValidator.validate(request))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void validate_throws_whenAddTrainingNameExceedsMaxLength() {
        var request = new Request.AddTraining(
                "John.Doe",
                credentialGenerator.generatePassword(),
                "Jane.Smith",
                "A".repeat(101),
                "YOGA",
                LocalDate.now(),
                60
        );

        assertThatThrownBy(() -> requestValidator.validate(request))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void validate_doesNotThrow_whenUpdateTraineeTrainersRequestIsValid() {
        var request = new Request.UpdateTraineeTrainers(
                "John.Doe",
                credentialGenerator.generatePassword(),
                List.of("Jane.Smith")
        );

        assertThatCode(() -> requestValidator.validate(request)).doesNotThrowAnyException();
    }

    @Test
    void validate_throws_whenUpdateTraineeTrainersHasEmptyList() {
        var request = new Request.UpdateTraineeTrainers(
                "John.Doe",
                credentialGenerator.generatePassword(),
                List.of()
        );

        assertThatThrownBy(() -> requestValidator.validate(request))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void validate_throws_whenUpdateTraineeTrainersHasBlankUsername() {
        var request = new Request.UpdateTraineeTrainers(
                "John.Doe",
                credentialGenerator.generatePassword(),
                List.of("")
        );

        assertThatThrownBy(() -> requestValidator.validate(request))
                .isInstanceOf(ConstraintViolationException.class);
    }
}
