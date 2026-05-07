package tech.provokedynamic.gymcrm.entity;

import org.immutables.value.Value;
import tech.provokedynamic.gymcrm.model.Address;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDate;

@Value.Immutable
@JsonDeserialize(builder = ImmutableTrainee.Builder.class)
public abstract non-sealed class Trainee extends User implements Entity {

    public static ImmutableTrainee.Builder builder() {
        return ImmutableTrainee.builder();
    }

    public abstract long id();

    public abstract Address address();

    public abstract LocalDate dateOfBirth();
}
