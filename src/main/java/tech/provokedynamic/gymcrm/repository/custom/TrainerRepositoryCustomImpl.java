package tech.provokedynamic.gymcrm.repository.custom;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.dto.Summary;
import tech.provokedynamic.gymcrm.entity.*;
import tech.provokedynamic.gymcrm.repository.TrainerRepositoryCustom;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TrainerRepositoryCustomImpl implements TrainerRepositoryCustom {

    private final EntityManager em;

    @Override
    public List<Summary.Training> findTrainingsByUsername(
            String username,
            @Nullable LocalDate from,
            @Nullable LocalDate to,
            @Nullable String trainee
    ) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Summary.Training.class);
        var root = cq.from(Training.class);

        Join<Training, Trainer> trainerJoin = root.join(Training_.trainer);
        Join<Training, Trainee> traineeJoin = root.join(Training_.trainee);

        cq.select(cb.construct(
                Summary.Training.class,
                root.get(Training_.trainingName),
                root.get(Training_.trainingDate),
                root.get(Training_.trainingDuration),
                traineeJoin.get(Trainee_.username)
        ));

        var predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(trainerJoin.get(Trainer_.username), username));

        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get(Training_.trainingDate), from));
        }
        if (to != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get(Training_.trainingDate), to));
        }
        if (trainee != null) {
            predicates.add(cb.equal(traineeJoin.get(Trainee_.username), trainee));
        }

        cq.where(predicates);
        cq.orderBy(cb.desc(root.get(Training_.trainingDate)));

        return em.createQuery(cq).getResultList();
    }
}
