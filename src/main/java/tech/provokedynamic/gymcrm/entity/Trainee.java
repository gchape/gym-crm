package tech.provokedynamic.gymcrm.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDeleteAction;
import tech.provokedynamic.gymcrm.model.Address;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@SuperBuilder(
        toBuilder = true)
@NoArgsConstructor(
        access = AccessLevel.PROTECTED)
@DiscriminatorValue(
        value = "trainee")
@org.hibernate.annotations.OnDelete(
        action = OnDeleteAction.CASCADE)
public class Trainee extends User {
    @ManyToMany
    @JoinTable(
            name = "trainee_trainer",
            joinColumns = @JoinColumn(name = "trainee_id"),
            inverseJoinColumns = @JoinColumn(name = "trainer_id")
    )
    private final Set<Trainer> trainers = new HashSet<>();

    @OneToMany(mappedBy = "trainee")
    private final Set<Training> trainings = new HashSet<>();

    @Nullable
    @Column(name = "dob")
    private LocalDate dateOfBirth;

    @Nullable
    @Embedded
    private Address address;
}
