package tech.provokedynamic.gymcrm.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.jpa.AvailableHints;
import tech.provokedynamic.gymcrm.model.Address;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@DiscriminatorValue("trainee")
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@org.hibernate.annotations.OnDelete(action = OnDeleteAction.CASCADE)
@NamedEntityGraph(
        name = "Trainee.withTrainers",
        attributeNodes = @NamedAttributeNode(value = "trainers", subgraph = "trainers.specialization"),
        subgraphs = @NamedSubgraph(
                name = "trainers.specialization",
                attributeNodes = @NamedAttributeNode("specialization")
        )
)
@NamedQuery(
        name = "Trainee.findWithTrainersByUsername",
        query = "SELECT t FROM Trainee t WHERE t.username = :username",
        hints = @QueryHint(name = AvailableHints.HINT_SPEC_FETCH_GRAPH, value = "Trainee.withTrainers")
)
public class Trainee extends User {
    @ManyToMany
    @JoinTable(
            name = "trainee_trainer",
            joinColumns = @JoinColumn(name = "trainee_id"),
            inverseJoinColumns = @JoinColumn(name = "trainer_id")
    )
    private final Set<Trainer> trainers = new HashSet<>();

    @Nullable
    @Column(name = "dob")
    private LocalDate dateOfBirth;

    @Nullable
    @Embedded
    private Address address;
}
