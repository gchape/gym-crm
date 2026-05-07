package tech.provokedynamic.gymcrm.entity;

import org.immutables.value.Value;
import tech.provokedynamic.gymcrm.model.Specialization;
import tools.jackson.databind.annotation.JsonDeserialize;

@Value.Immutable
@JsonDeserialize(builder = ImmutableTrainer.Builder.class)
public abstract non-sealed class Trainer extends User implements Entity {

    public static ImmutableTrainer.Builder builder() {
        return ImmutableTrainer.builder();
    }

    public abstract long id();

    public abstract Specialization specialization();
}
