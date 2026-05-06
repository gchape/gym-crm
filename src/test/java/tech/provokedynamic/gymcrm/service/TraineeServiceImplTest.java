package tech.provokedynamic.gymcrm.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.provokedynamic.gymcrm.aspect.ValidationAspect;
import tech.provokedynamic.gymcrm.component.CredentialGenerator;
import tech.provokedynamic.gymcrm.component.InMemoryStorage;
import tech.provokedynamic.gymcrm.dao.TraineeDao;
import tech.provokedynamic.gymcrm.dto.TraineeRequest;
import tech.provokedynamic.gymcrm.dto.TraineeResponse;
import tech.provokedynamic.gymcrm.model.Address;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        TraineeServiceImpl.class,
        TraineeServiceImplTest.TestConfig.class,
        TraineeDao.class,
        CredentialGenerator.class,
        ValidationAspect.class,
        InMemoryStorage.class
})
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
        TraineeResponse.Detail result = traineeService.create(validCreateRequest());

        assertThat(result).isNotNull();
        assertThat(result.username()).isNotBlank();
    }

    @Test
    void shouldThrowException_whenFirstNameIsBlank() {
        TraineeRequest.Create request = new TraineeRequest.Create(
                "", "Doe", LocalDate.of(1995, 5, 10), validAddress()
        );

        assertThatThrownBy(() -> traineeService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenLastNameIsBlank() {
        TraineeRequest.Create request = new TraineeRequest.Create(
                "John", "", LocalDate.of(1995, 5, 10), validAddress()
        );

        assertThatThrownBy(() -> traineeService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenDateOfBirthIsInTheFuture() {
        TraineeRequest.Create request = new TraineeRequest.Create(
                "John", "Doe", LocalDate.now().plusDays(1), validAddress()
        );

        assertThatThrownBy(() -> traineeService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenDateOfBirthIsToday() {
        TraineeRequest.Create request = new TraineeRequest.Create(
                "John", "Doe", LocalDate.now(), validAddress()
        );

        assertThatThrownBy(() -> traineeService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenAddressIsNull() {
        TraineeRequest.Create request = new TraineeRequest.Create(
                "Jane", "Smith", LocalDate.of(1990, 3, 15), null
        );

        assertThatThrownBy(() -> traineeService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldCreateTrainee_whenAddressIsFullyValid() {
        TraineeResponse.Detail result = traineeService.create(validCreateRequest());

        assertThat(result.address()).isEqualTo(validAddress());
        assertThat(result.address().street()).isEqualTo("Baker Street 221B");
        assertThat(result.address().city()).isEqualTo("London");
        assertThat(result.address().country()).isEqualTo("UK");
        assertThat(result.address().postalCode()).isEqualTo("12345");
    }

    @Test
    void shouldThrowException_whenAddressStreetIsBlank() {
        Address address = new Address("", "London", "UK", "12345");
        TraineeRequest.Create request = new TraineeRequest.Create(
                "Jane", "Smith", LocalDate.of(1990, 3, 15), address
        );

        assertThatThrownBy(() -> traineeService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenAddressCityIsBlank() {
        Address address = new Address("Baker Street 221B", "", "UK", "12345");
        TraineeRequest.Create request = new TraineeRequest.Create(
                "Jane", "Smith", LocalDate.of(1990, 3, 15), address
        );

        assertThatThrownBy(() -> traineeService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenAddressCountryIsBlank() {
        Address address = new Address("Baker Street 221B", "London", "", "12345");
        TraineeRequest.Create request = new TraineeRequest.Create(
                "Jane", "Smith", LocalDate.of(1990, 3, 15), address
        );

        assertThatThrownBy(() -> traineeService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenPostalCodeIsNull() {
        Address address = new Address("Baker Street 221B", "London", "UK", null);
        TraineeRequest.Create request = new TraineeRequest.Create(
                "Jane", "Smith", LocalDate.of(1990, 3, 15), address
        );

        assertThatThrownBy(() -> traineeService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenPostalCodeIsTooShort() {
        Address address = new Address("Baker Street 221B", "London", "UK", "123");
        TraineeRequest.Create request = new TraineeRequest.Create(
                "Jane", "Smith", LocalDate.of(1990, 3, 15), address
        );

        assertThatThrownBy(() -> traineeService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenPostalCodeIsTooLong() {
        Address address = new Address("Baker Street 221B", "London", "UK", "12345678901");
        TraineeRequest.Create request = new TraineeRequest.Create(
                "Jane", "Smith", LocalDate.of(1990, 3, 15), address
        );

        assertThatThrownBy(() -> traineeService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenPostalCodeContainsLetters() {
        Address address = new Address("Baker Street 221B", "London", "UK", "SW1A1AA");
        TraineeRequest.Create request = new TraineeRequest.Create(
                "Jane", "Smith", LocalDate.of(1990, 3, 15), address
        );

        assertThatThrownBy(() -> traineeService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldCreateTrainee_whenPostalCodeIsAtMinimumLength() {
        Address address = new Address("Baker Street 221B", "London", "UK", "1234");
        TraineeRequest.Create request = new TraineeRequest.Create(
                "Jane", "Smith", LocalDate.of(1990, 3, 15), address
        );

        TraineeResponse.Detail result = traineeService.create(request);

        assertThat(result.address().postalCode()).isEqualTo("1234");
    }

    @Test
    void shouldCreateTrainee_whenPostalCodeIsAtMaximumLength() {
        Address address = new Address("Baker Street 221B", "London", "UK", "1234567890");
        TraineeRequest.Create request = new TraineeRequest.Create(
                "Jane", "Smith", LocalDate.of(1990, 3, 15), address
        );

        TraineeResponse.Detail result = traineeService.create(request);

        assertThat(result.address().postalCode()).isEqualTo("1234567890");
    }

    @Test
    void shouldUpdateTrainee_whenRequestIsValid() {
        TraineeResponse.Detail created = traineeService.create(validCreateRequest());

        TraineeRequest.Update update = new TraineeRequest.Update(
                "Johnny", "Doe", LocalDate.of(1995, 5, 10), validAddress(), true
        );

        TraineeResponse.Detail updated = traineeService.update(created.id(), update);

        assertThat(updated.firstName()).isEqualTo("Johnny");
        assertThat(updated.isActive()).isTrue();
    }

    @Test
    void shouldUpdateTrainee_whenAddressChangesToValidValue() {
        TraineeResponse.Detail created = traineeService.create(validCreateRequest());

        Address newAddress = new Address("New Street 99", "Munich", "Germany", "80331");
        TraineeRequest.Update update = new TraineeRequest.Update(
                "John", "Doe", LocalDate.of(1995, 5, 10), newAddress, true
        );

        TraineeResponse.Detail updated = traineeService.update(created.id(), update);

        assertThat(updated.address().street()).isEqualTo("New Street 99");
        assertThat(updated.address().city()).isEqualTo("Munich");
        assertThat(updated.address().postalCode()).isEqualTo("80331");
    }

    @Test
    void shouldThrowException_whenUpdatingTraineeWithInvalidAddress() {
        TraineeResponse.Detail created = traineeService.create(validCreateRequest());

        Address badAddress = new Address("", "Munich", "Germany", "80331");
        TraineeRequest.Update update = new TraineeRequest.Update(
                "John", "Doe", LocalDate.of(1995, 5, 10), badAddress, true
        );

        assertThatThrownBy(() -> traineeService.update(created.id(), update))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenUpdatingNonExistentTrainee() {
        TraineeRequest.Update update = new TraineeRequest.Update(
                "John", "Doe", LocalDate.of(1995, 5, 10), validAddress(), true
        );

        assertThatThrownBy(() -> traineeService.update(999999L, update))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldFindTraineeById_whenTraineeExists() {
        TraineeResponse.Detail created = traineeService.create(validCreateRequest());

        TraineeResponse.Detail found = traineeService.findById(created.id());

        assertThat(found).isEqualTo(created);
    }

    @Test
    void shouldThrowException_whenFindByIdAndTraineeDoesNotExist() {
        assertThatThrownBy(() -> traineeService.findById(999999L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldReturnAllTrainees_whenFindAllCalled() {
        traineeService.create(validCreateRequest());
        traineeService.create(new TraineeRequest.Create(
                "Jane", "Smith", LocalDate.of(1990, 3, 15), validAddress()
        ));

        List<TraineeResponse.Summary> result = traineeService.findAll();

        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldDeleteTrainee_whenTraineeExists() {
        TraineeResponse.Detail created = traineeService.create(validCreateRequest());

        traineeService.delete(created.id());

        assertThatThrownBy(() -> traineeService.findById(created.id()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {
    }
}