package tech.provokedynamic.gymcrm.repository;

import org.springframework.data.jpa.domain.Specification;
import tech.provokedynamic.gymcrm.dto.TrainingSearch;
import tech.provokedynamic.gymcrm.entity.*;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;

/** Composes participant visibility and optional session filters into a repository specification. */
public final class TrainingSpecifications {
    private TrainingSpecifications() { }

    public static Specification<Training> visibleTo(String username, TrainingSearch search) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            var trainee = root.join(Training_.trainee);
            var trainer = root.join(Training_.trainer);
            predicates.add(cb.or(cb.equal(trainee.get(Trainee_.username), username),
                    cb.equal(trainer.get(Trainer_.username), username)));
            if (search.from() != null) predicates.add(cb.greaterThanOrEqualTo(root.get(Training_.trainingDate), search.from()));
            if (search.to() != null) predicates.add(cb.lessThanOrEqualTo(root.get(Training_.trainingDate), search.to()));
            if (search.traineeUsername() != null) predicates.add(cb.equal(trainee.get(Trainee_.username), search.traineeUsername()));
            if (search.trainerUsername() != null) predicates.add(cb.equal(trainer.get(Trainer_.username), search.trainerUsername()));
            if (search.trainingType() != null) predicates.add(cb.equal(root.get(Training_.trainingType).get(TrainingType_.trainingTypeName), search.trainingType()));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
