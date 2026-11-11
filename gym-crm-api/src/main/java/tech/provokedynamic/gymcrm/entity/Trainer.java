package tech.provokedynamic.gymcrm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@SuperBuilder(
        toBuilder = true)
@NoArgsConstructor(
        access = AccessLevel.PROTECTED)
@DiscriminatorValue(
        value = "trainer")
public class Trainer extends User {
    @ManyToMany(mappedBy = "trainers")
    private final Set<Trainee> trainees = new HashSet<>();

    @OneToMany(mappedBy = "trainer")
    private final Set<Training> trainings = new HashSet<>();

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_type_id")
    private TrainingType specialization;
}
