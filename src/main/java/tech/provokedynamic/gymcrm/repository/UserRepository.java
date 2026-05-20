package tech.provokedynamic.gymcrm.repository;

import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.entity.User;

@Repository
public interface UserRepository extends BaseUserRepository<User> {
}
