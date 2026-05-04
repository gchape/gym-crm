package tech.provokedynamic.gymcrm.entity;

import tech.provokedynamic.gymcrm.model.Specialization;

public final class Trainer extends User implements Entity {
    private final long id;
    private final Specialization specialization;

    private Trainer(Builder builder) {
        super(builder);
        this.id = builder.id;
        this.specialization = builder.specialization;
    }

    public static Builder builder() {
        return new Builder();
    }

    public long getId() {
        return id;
    }

    public Specialization getSpecialization() {
        return specialization;
    }

    public static class Builder extends User.Builder<Builder> {
        private long id;
        private Specialization specialization;

        private Builder() {
        }

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public Builder specialization(Specialization specialization) {
            this.specialization = specialization;
            return this;
        }

        @Override
        public Trainer build() {
            return new Trainer(this);
        }
    }
}
