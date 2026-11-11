package tech.provokedynamic.gymcrm.repository;

import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import tech.provokedynamic.gymcrm.entity.User;

import java.util.Optional;

public interface BaseUserRepository<T extends User> extends JpaRepository<T, Long> {

    boolean existsByUsername(String username);

    Optional<T> findByUsername(String username);

    boolean existsByUsernameAndPassword(String username, String password);

    Optional<T> findByUsernameAndPassword(String username, String password);

    @Query(value = "SELECT count(*) > 0 FROM \"user\" WHERE username = :username", nativeQuery = true)
    boolean existsByUsernameIncludingDeleted(@Param("username") String username);

    @Modifying
    @Query(value = "UPDATE \"user\" SET is_active = false WHERE username = :username AND is_active = true", nativeQuery = true)
    @QueryHints(value = @QueryHint(
            name = "org.hibernate.query.native.spaces",
            value = "User"))
    int deactivateByUsername(@Param("username") String username);

    @Modifying
    @Query(value = "UPDATE \"user\" SET is_active = true WHERE username = :username AND is_active = false", nativeQuery = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.query.native.spaces", value = "User"))
    int activateByUsername(@Param("username") String username);
}
