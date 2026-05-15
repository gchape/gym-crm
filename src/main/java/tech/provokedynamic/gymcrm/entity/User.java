package tech.provokedynamic.gymcrm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import static jakarta.persistence.GenerationType.SEQUENCE;
import static lombok.AccessLevel.PROTECTED;
import static org.hibernate.annotations.SoftDeleteType.ACTIVE;

@Entity
@Table(name = "\"user\"")
@lombok.Getter
@lombok.NoArgsConstructor(access = PROTECTED)
@org.hibernate.annotations.SoftDelete(strategy = ACTIVE, columnName = "is_active")
public class User {
    @Id
    @GeneratedValue(strategy = SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(length = 50)
    private String firstName;

    @NotNull
    @Column(length = 50)
    private String lastName;

    @NotNull
    @Column(length = 128, unique = true)
    private String username;

    @NotNull
    @Column(length = 10)
    private String password;

    public static User of(String firstName, String lastName, String username, String password) {
        var user = new User();
        user.firstName = firstName;
        user.lastName = lastName;
        user.username = username;
        user.password = password;
        return user;
    }
}
