package tech.provokedynamic.gymcrm.entity;

import org.immutables.value.Value;
import tech.provokedynamic.gymcrm.model.TrainingType;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.time.Duration;
import java.time.LocalDate;

@Value.Immutable
@JsonDeserialize(builder = ImmutableTraining.Builder.class)
public abstract non-sealed class Training implements Entity {

    public static ImmutableTraining.Builder builder() {
        return ImmutableTraining.builder();
    }

    public abstract long id();

    public abstract long traineeId();

    public abstract long trainerId();

    public abstract String trainingName();

    public abstract TrainingType trainingType();

    public abstract LocalDate trainingDate();

    public abstract Duration trainingDuration();
}
