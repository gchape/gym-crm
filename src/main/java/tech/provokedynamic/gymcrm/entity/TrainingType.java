package tech.provokedynamic.gymcrm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import tech.provokedynamic.gymcrm.model.TrainingTypeName;

@Entity
@Table(name = "training_type")
@lombok.Getter(AccessLevel.PUBLIC)
@lombok.NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class TrainingType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(length = 50, unique = true)
    @Enumerated(EnumType.STRING)
    private TrainingTypeName name;

    public static TrainingType of(TrainingTypeName name) {
        var trainingType = new TrainingType();
        trainingType.name = name;
        return trainingType;
    }
}
