package tech.provokedynamic.gymcrm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(
        access = PROTECTED)
@org.hibernate.annotations.Immutable
public class TrainingType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @NotNull
    @Column(unique = true)
    private String trainingTypeName;

    public TrainingType(String trainingTypeName) {
        this.trainingTypeName = trainingTypeName;
    }
}
