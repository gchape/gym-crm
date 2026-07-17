package tech.provokedynamic.gymcrmauthorizationserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.provokedynamic.gymcrmauthorizationserver.entity.AuthUser;

import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {

    Optional<AuthUser> findByUsernameAndActiveTrue(String username);
}
