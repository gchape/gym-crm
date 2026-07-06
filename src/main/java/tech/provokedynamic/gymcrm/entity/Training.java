package tech.provokedynamic.gymcrm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Builder
@NoArgsConstructor(
        access = AccessLevel.PROTECTED)
@AllArgsConstructor(
        access = AccessLevel.PRIVATE)
@EqualsAndHashCode(
        onlyExplicitlyIncluded = true,
        cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY)
public class Training {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @NotNull
    private String trainingName;

    @NotNull
    private LocalDate trainingDate;

    @NotNull
    private Integer trainingDuration;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainee_id")
    private Trainee trainee;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainer_id")
    private Trainer trainer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_type_id")
    private TrainingType trainingType;
}
