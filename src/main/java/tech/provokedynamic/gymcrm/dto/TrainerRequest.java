package tech.provokedynamic.gymcrm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tech.provokedynamic.gymcrm.model.Specialization;

public abstract sealed class TrainerRequest implements Request permits TrainerRequest.Create, TrainerRequest.Update {
    @NotBlank(message = "First name is required")
    private final String firstName;

    @NotBlank(message = "Last name is required")
    private final String lastName;

    @NotNull(message = "Specialization is required")
    private final Specialization specialization;

    protected TrainerRequest(String firstName, String lastName, Specialization specialization) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Specialization getSpecialization() {
        return specialization;
    }

    public static final class Create extends TrainerRequest {
        public Create(String firstName, String lastName, Specialization specialization) {
            super(firstName, lastName, specialization);
        }
    }

    public static final class Update extends TrainerRequest {
        private final boolean isActive;

        public Update(String firstName, String lastName, Specialization specialization, boolean isActive) {
            super(firstName, lastName, specialization);
            this.isActive = isActive;
        }

        public boolean isActive() {
            return isActive;
        }
    }
}
