package tech.provokedynamic.gymcrm.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import tech.provokedynamic.gymcrm.model.Address;

import java.time.LocalDate;

public abstract sealed class TraineeRequest implements Request permits TraineeRequest.Create, TraineeRequest.Update {
    @NotBlank(message = "First name is required")
    private final String firstName;

    @NotBlank(message = "Last name is required")
    private final String lastName;

    @Past(message = "Date of birth must be in the past")
    private final LocalDate dateOfBirth;

    @Valid
    @NotNull(message = "Address is required")
    private final Address address;

    protected TraineeRequest(String firstName, String lastName, LocalDate dateOfBirth, Address address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public Address getAddress() {
        return address;
    }

    public static final class Create extends TraineeRequest {
        public Create(String firstName, String lastName, LocalDate dateOfBirth, Address address) {
            super(firstName, lastName, dateOfBirth, address);
        }
    }

    public static final class Update extends TraineeRequest {
        private final boolean isActive;

        public Update(String firstName, String lastName, LocalDate dateOfBirth, Address address, boolean isActive) {
            super(firstName, lastName, dateOfBirth, address);
            this.isActive = isActive;
        }

        public boolean isActive() {
            return isActive;
        }
    }
}
