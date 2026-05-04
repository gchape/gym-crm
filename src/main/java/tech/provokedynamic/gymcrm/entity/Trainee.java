package tech.provokedynamic.gymcrm.entity;

import tech.provokedynamic.gymcrm.model.Address;

import java.time.LocalDate;

public final class Trainee extends User implements Entity {
    private final long id;
    private final Address address;
    private final LocalDate dateOfBirth;

    private Trainee(Builder builder) {
        super(builder);
        this.dateOfBirth = builder.dateOfBirth;
        this.address = builder.address;
        this.id = builder.id;
    }

    public static Builder builder() {
        return new Builder();
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public Address getAddress() {
        return address;
    }

    public long getId() {
        return id;
    }

    public static class Builder extends User.Builder<Builder> {
        private LocalDate dateOfBirth;
        private Address address;
        private long id;

        private Builder() {
        }

        public Builder dateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder address(Address address) {
            this.address = address;
            return this;
        }

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        @Override
        public Trainee build() {
            return new Trainee(this);
        }
    }
}
