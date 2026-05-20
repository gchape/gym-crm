package tech.provokedynamic.gymcrm.repository;

import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.entity.Trainer;

import java.util.Collection;
import java.util.List;

@Repository
public interface TrainerRepository extends BaseUserRepository<Trainer>, TrainerRepositoryCustom {

    List<Trainer> findAllByIdNotIn(Collection<Long> ids);

    List<Trainer> findAllByUsernameIn(Collection<String> usernames);
}
