package tech.provokedynamic.gymcrm.repository.custom;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.dto.Summary;
import tech.provokedynamic.gymcrm.entity.*;
import tech.provokedynamic.gymcrm.repository.TraineeRepositoryCustom;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TraineeRepositoryCustomImpl implements TraineeRepositoryCustom {

    private final EntityManager em;

    @Override
    public List<Summary.Training> findTrainingsByUsername(
            String username,
            @Nullable LocalDate from,
            @Nullable LocalDate to,
            @Nullable String trainer,
            @Nullable String type
    ) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Summary.Training.class);
        var root = cq.from(Training.class);

        Join<Training, Trainee> traineeJoin = root.join(Training_.trainee);
        Join<Training, Trainer> trainerJoin = root.join(Training_.trainer);

        cq.select(cb.construct(
                Summary.Training.class,
                root.get(Training_.trainingName),
                root.get(Training_.trainingDate),
                root.get(Training_.trainingDuration),
                trainerJoin.get(Trainer_.username)
        ));

        var predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(traineeJoin.get(Trainee_.username), username));

        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get(Training_.trainingDate), from));
        }
        if (to != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get(Training_.trainingDate), to));
        }
        if (trainer != null) {
            predicates.add(cb.equal(trainerJoin.get(Trainer_.username), trainer));
        }
        if (type != null) {
            Join<Training, TrainingType> typeJoin = root.join(Training_.trainingType);
            predicates.add(cb.equal(typeJoin.get(TrainingType_.trainingTypeName), type));
        }

        cq.where(predicates);
        cq.orderBy(cb.desc(root.get(Training_.trainingDate)));

        return em.createQuery(cq).getResultList();
    }
}
