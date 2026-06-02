package tech.provokedynamic.gymcrm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import static jakarta.persistence.GenerationType.SEQUENCE;
import static org.hibernate.annotations.SoftDeleteType.ACTIVE;

@Entity
@Table(name = "\"user\"")
@Getter
@SuperBuilder(
        toBuilder = true)
@NoArgsConstructor(
        access = AccessLevel.PROTECTED)
@EqualsAndHashCode(
        onlyExplicitlyIncluded = true,
        cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY)
@Inheritance(
        strategy = InheritanceType.JOINED)
@DiscriminatorColumn(
        name = "u_type",
        discriminatorType = DiscriminatorType.STRING)
@org.hibernate.annotations.SoftDelete(
        strategy = ACTIVE,
        columnName = "is_active")
public abstract class User {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = SEQUENCE)
    @Column(nullable = false, updatable = false)
    private Long id;

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @NotNull
    @Column(unique = true)
    private String username;

    @NotNull
    @Setter
    private String password;
}
