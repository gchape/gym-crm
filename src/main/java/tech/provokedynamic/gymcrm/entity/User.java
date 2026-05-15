package tech.provokedynamic.gymcrm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import static jakarta.persistence.GenerationType.SEQUENCE;
import static org.hibernate.annotations.SoftDeleteType.ACTIVE;

@Entity
@Table(name = "\"user\"")
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, name = "u_type")
@org.hibernate.annotations.SoftDelete(strategy = ACTIVE, columnName = "is_active")
public abstract class User {
    @Id
    @GeneratedValue(strategy = SEQUENCE)
    @Column(nullable = false, insertable = false)
    private Long id;

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @NotNull
    @Column(unique = true)
    private String username;

    @NotNull
    private String password;
}
