package tech.provokedynamic.gymcrmauthorizationserver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

/**
 * Read-only view of the "user" table owned by gym-crm. This module does not
 * run Flyway and never writes through this entity — gym-crm remains the
 * single writer of user data. Only the columns needed for authentication
 * are mapped.
 */
@Entity
@Table(name = "\"user\"")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class AuthUser {

    @Id
    private Long id;

    private String username;

    private String password;

    @Column(name = "is_active")
    private boolean active;

    // Discriminator column populated by gym-crm's @DiscriminatorValue
    // ("trainee" / "trainer") — read-only here.
    @Column(name = "u_type", insertable = false, updatable = false)
    private String userType;
}
