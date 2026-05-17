package tech.provokedynamic.gymcrm.dao.impl;

import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.jpa.AvailableHints;
import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.dao.TraineeDao;
import tech.provokedynamic.gymcrm.dto.Profile;
import tech.provokedynamic.gymcrm.dto.Summary;
import tech.provokedynamic.gymcrm.entity.Trainee;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.entity.Trainer_;
import tech.provokedynamic.gymcrm.entity.TrainingType_;
import tech.provokedynamic.gymcrm.model.UserType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public final class TraineeDaoImpl extends AbstractUserDaoImpl implements TraineeDao {

    private static final String EXISTS_BY_USERNAME =
            "SELECT count(t.id) > 0 FROM Trainee t WHERE t.username = :username";

    private static final String FIND_BY_USERNAME =
            "SELECT t FROM Trainee t WHERE t.username = :username";

    private static final String NAMED_FIND_WITH_TRAINERS =
            "Trainee.findWithTrainersByUsername";

    public TraineeDaoImpl(EntityManager em) {
        super(em);
    }

    @Override
    public boolean existsByUsername(String username) {
        return em.createQuery(EXISTS_BY_USERNAME, Boolean.class)
                .setParameter("username", username)
                .setHint(AvailableHints.HINT_READ_ONLY, true)
                .getSingleResult();
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        try {
            return Optional.of(
                    em.createQuery(FIND_BY_USERNAME, Trainee.class)
                            .setParameter("username", username)
                            .getSingleResult()
            );
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public void delete(Trainee trainee) {
        em.remove(em.contains(trainee) ? trainee : em.merge(trainee));
    }

    @Override
    public List<Summary.Training> findTrainingsByUsername(
            String username,
            @Nullable LocalDate from,
            @Nullable LocalDate to,
            @Nullable String trainer,
            @Nullable String type
    ) {
        return super.findTrainingsByUsername(
                username,
                UserType.TRAINEE,
                from,
                to,
                trainer,
                type
        );
    }

    @Override
    public List<Profile.Trainer> findUnassignedTrainers(String username) {
        Set<Trainer> assignedTrainers = findTraineeWithTrainers(username).getTrainers();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Profile.Trainer> cq = cb.createQuery(Profile.Trainer.class);
        Root<Trainer> root = cq.from(Trainer.class);

        cq.select(cb.construct(
                Profile.Trainer.class,
                root.get(Trainer_.firstName),
                root.get(Trainer_.lastName),
                root.get(Trainer_.username),
                root.get(Trainer_.specialization)
                        .get(TrainingType_.trainingTypeName)
        ));

        if (!assignedTrainers.isEmpty()) {
            cq.where(cb.not(root.in(assignedTrainers)));
        }

        return em.createQuery(cq).getResultList();
    }

    @Override
    public List<Profile.Trainer> findAssignedTrainers(String username) {
        return findTraineeWithTrainers(username)
                .getTrainers()
                .stream()
                .map(Profile.Trainer::from)
                .toList();
    }

    private Trainee findTraineeWithTrainers(String username) {
        return em.createNamedQuery(NAMED_FIND_WITH_TRAINERS, Trainee.class)
                .setParameter("username", username)
                .getSingleResult();
    }
}
