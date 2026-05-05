package tech.provokedynamic.gymcrm.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import tech.provokedynamic.gymcrm.aspect.ValidationAspect;
import tech.provokedynamic.gymcrm.component.CredentialGenerator;
import tech.provokedynamic.gymcrm.component.InMemoryStorage;
import tech.provokedynamic.gymcrm.dao.TraineeDao;
import tech.provokedynamic.gymcrm.dto.TraineeRequest;
import tech.provokedynamic.gymcrm.entity.Trainee;
import tech.provokedynamic.gymcrm.model.Address;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = {
        TraineeServiceImpl.class,
        TraineeDao.class,
        CredentialGenerator.class,
        ValidationAspect.class,
        InMemoryStorage.class
}, webEnvironment = SpringBootTest.WebEnvironment.NONE,
        useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@EnableAspectJAutoProxy
class TraineeServiceImplTest {

    @Autowired
    private TraineeService traineeService;

    private Address validAddress() {
        return new Address("Baker Street 221B", "London", "UK", "12345");
    }

    private TraineeRequest.Create validCreateRequest() {
        return new TraineeRequest.Create(
                "John", "Doe",
                LocalDate.of(1995, 5, 10),
                validAddress()
        );
    }

    @Test
    void shouldCreateTrainee_whenRequestIsValid() {
        Trainee result = traineeService.create(validCreateRequest());

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isNotBlank();
        assertThat(result.getPassword()).hasSize(10);
    }

    @Test
    void shouldThrowException_whenFirstNameIsBlank() {
        TraineeRequest.Create request =
                new TraineeRequest.Create(
                        "", "Doe",
                        LocalDate.of(1995, 5, 10),
                        validAddress()
                );

        assertThatThrownBy(() -> traineeService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenLastNameIsBlank() {
        TraineeRequest.Create request =
                new TraineeRequest.Create(
                        "John", "",
                        LocalDate.of(1995, 5, 10),
                        validAddress()
                );

        assertThatThrownBy(() -> traineeService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenDateOfBirthIsInTheFuture() {
        TraineeRequest.Create request =
                new TraineeRequest.Create(
                        "John", "Doe",
                        LocalDate.now().plusDays(1),
                        validAddress()
                );

        assertThatThrownBy(() -> traineeService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenDateOfBirthIsToday() {
        TraineeRequest.Create request =
                new TraineeRequest.Create(
                        "John", "Doe",
                        LocalDate.now(),
                        validAddress()
                );

        assertThatThrownBy(() -> traineeService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenAddressIsNull() {
        TraineeRequest.Create request =
                new TraineeRequest.Create(
                        "Jane", "Smith",
                        LocalDate.of(1990, 3, 15),
                        null
                );

        assertThatThrownBy(() -> traineeService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldCreateTrainee_whenAddressIsFullyValid() {
        Trainee result = traineeService.create(validCreateRequest());

        assertThat(result.getAddress()).isEqualTo(validAddress());
        assertThat(result.getAddress().street()).isEqualTo("Baker Street 221B");
        assertThat(result.getAddress().city()).isEqualTo("London");
        assertThat(result.getAddress().country()).isEqualTo("UK");
        assertThat(result.getAddress().postalCode()).isEqualTo("12345");
    }

    @Test
    void shouldThrowException_whenAddressStreetIsBlank() {
        Address address = new Address("", "London", "UK", "12345");
        TraineeRequest.Create request =
                new TraineeRequest.Create(
                        "Jane", "Smith",
                        LocalDate.of(1990, 3, 15),
                        address
                );

        assertThatThrownBy(() -> traineeService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenAddressCityIsBlank() {
        Address address = new Address("Baker Street 221B", "", "UK", "12345");
        TraineeRequest.Create request =
                new TraineeRequest.Create(
                        "Jane", "Smith",
                        LocalDate.of(1990, 3, 15),
                        address
                );

        assertThatThrownBy(() -> traineeService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenAddressCountryIsBlank() {
        Address address = new Address("Baker Street 221B", "London", "", "12345");
        TraineeRequest.Create request =
                new TraineeRequest.Create(
                        "Jane", "Smith",
                        LocalDate.of(1990, 3, 15),
                        address
                );

        assertThatThrownBy(() -> traineeService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenPostalCodeIsNull() {
        Address address = new Address("Baker Street 221B", "London", "UK", null);
        TraineeRequest.Create request =
                new TraineeRequest.Create(
                        "Jane", "Smith",
                        LocalDate.of(1990, 3, 15),
                        address
                );

        assertThatThrownBy(() -> traineeService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenPostalCodeIsTooShort() {
        Address address = new Address("Baker Street 221B", "London", "UK", "123");
        TraineeRequest.Create request =
                new TraineeRequest.Create(
                        "Jane", "Smith",
                        LocalDate.of(1990, 3, 15),
                        address
                );

        assertThatThrownBy(() -> traineeService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenPostalCodeIsTooLong() {
        Address address = new Address("Baker Street 221B", "London", "UK", "12345678901");
        TraineeRequest.Create request =
                new TraineeRequest.Create(
                        "Jane", "Smith",
                        LocalDate.of(1990, 3, 15),
                        address
                );

        assertThatThrownBy(() -> traineeService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenPostalCodeContainsLetters() {
        Address address = new Address("Baker Street 221B", "London", "UK", "SW1A1AA");
        TraineeRequest.Create request =
                new TraineeRequest.Create(
                        "Jane", "Smith",
                        LocalDate.of(1990, 3, 15),
                        address
                );

        assertThatThrownBy(() -> traineeService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldCreateTrainee_whenPostalCodeIsAtMinimumLength() {
        Address address = new Address("Baker Street 221B", "London", "UK", "1234");
        TraineeRequest.Create request =
                new TraineeRequest.Create(
                        "Jane", "Smith",
                        LocalDate.of(1990, 3, 15),
                        address
                );

        Trainee result = traineeService.create(request);

        assertThat(result.getAddress().postalCode()).isEqualTo("1234");
    }

    @Test
    void shouldCreateTrainee_whenPostalCodeIsAtMaximumLength() {
        Address address = new Address("Baker Street 221B", "London", "UK", "1234567890");
        TraineeRequest.Create request =
                new TraineeRequest.Create(
                        "Jane", "Smith",
                        LocalDate.of(1990, 3, 15),
                        address
                );

        Trainee result = traineeService.create(request);

        assertThat(result.getAddress().postalCode()).isEqualTo("1234567890");
    }

    @Test
    void shouldUpdateTrainee_whenRequestIsValid() {
        Trainee created = traineeService.create(validCreateRequest());

        TraineeRequest.Update update =
                new TraineeRequest.Update(
                        "Johnny", "Doe",
                        LocalDate.of(1995, 5, 10),
                        validAddress(),
                        true
                );

        Trainee updated = traineeService.update(created.getId(), update);

        assertThat(updated.getFirstName()).isEqualTo("Johnny");
        assertThat(updated.isActive()).isTrue();
    }

    @Test
    void shouldUpdateTrainee_whenAddressChangesToValidValue() {
        Trainee created = traineeService.create(validCreateRequest());

        Address newAddress = new Address("New Street 99", "Munich", "Germany", "80331");
        TraineeRequest.Update update =
                new TraineeRequest.Update(
                        "John", "Doe",
                        LocalDate.of(1995, 5, 10),
                        newAddress,
                        true
                );

        Trainee updated = traineeService.update(created.getId(), update);

        assertThat(updated.getAddress().street()).isEqualTo("New Street 99");
        assertThat(updated.getAddress().city()).isEqualTo("Munich");
        assertThat(updated.getAddress().postalCode()).isEqualTo("80331");
    }

    @Test
    void shouldThrowException_whenUpdatingTraineeWithInvalidAddress() {
        Trainee created = traineeService.create(validCreateRequest());

        Address badAddress = new Address("", "Munich", "Germany", "80331");
        TraineeRequest.Update update =
                new TraineeRequest.Update(
                        "John", "Doe",
                        LocalDate.of(1995, 5, 10),
                        badAddress,
                        true
                );

        assertThatThrownBy(() -> traineeService.update(created.getId(), update))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenUpdatingNonExistentTrainee() {
        TraineeRequest.Update update =
                new TraineeRequest.Update(
                        "John", "Doe",
                        LocalDate.of(1995, 5, 10),
                        validAddress(),
                        true
                );

        assertThatThrownBy(() -> traineeService.update(999999L, update))
                .isInstanceOf(IllegalArgumentException.class);
    }
}