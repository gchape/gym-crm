package tech.provokedynamic.gymcrm.dao.impl;

import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.*;
import org.hibernate.jpa.AvailableHints;
import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.dao.TraineeDao;
import tech.provokedynamic.gymcrm.dto.Profile;
import tech.provokedynamic.gymcrm.dto.Summary;
import tech.provokedynamic.gymcrm.entity.Trainee;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.entity.Training;
import tech.provokedynamic.gymcrm.entity.TrainingType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class TraineeDaoImpl extends UserDaoImpl implements TraineeDao {

    public TraineeDaoImpl(EntityManager em) {
        super(em);
    }

    @Override
    public void save(Trainee trainee) {
        em.persist(trainee);
    }

    @Override
    public void update(Trainee trainee) {
        em.merge(trainee);
    }

    @Override
    public boolean existsByUsername(String username) {
        return em.createQuery(
                        "SELECT count(t.id) > 0 FROM Trainee t WHERE t.username = :username",
                        Boolean.class)
                .setParameter("username", username)
                .setHint(AvailableHints.HINT_READ_ONLY, true)
                .getSingleResult();
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        try {
            return Optional.of(
                    em.createQuery(
                                    "SELECT t FROM Trainee t WHERE t.username = :username",
                                    Trainee.class)
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
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Summary.Training> cq = cb.createQuery(Summary.Training.class);
        Root<Training> root = cq.from(Training.class);

        Join<Training, Trainee> traineeJoin = root.join("trainee");
        Join<Training, Trainer> trainerJoin = root.join("trainer");
        Join<Training, TrainingType> typeJoin = root.join("trainingType");

        cq.select(cb.construct(Summary.Training.class,
                root.get("trainingName"),
                root.get("trainingDate"),
                root.get("trainingDuration"),
                trainerJoin.get("username")
        ));

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(traineeJoin.get("username"), username));

        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("trainingDate"), from));
        }
        if (to != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("trainingDate"), to));
        }
        if (trainer != null) {
            predicates.add(cb.equal(trainerJoin.get("username"), trainer));
        }
        if (type != null) {
            predicates.add(cb.equal(typeJoin.get("trainingTypeName"), type));
        }

        cq.where(predicates);
        cq.orderBy(cb.desc(root.get("trainingDate")));

        return em.createQuery(cq).getResultList();
    }

    @Override
    public List<Profile.Trainer> findUnassignedTrainers(String username) {
        Set<Trainer> assignedTrainers = em.createNamedQuery(
                        "Trainee.findWithTrainersByUsername",
                        Trainee.class)
                .setParameter("username", username)
                .getSingleResult()
                .getTrainers();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Profile.Trainer> cq = cb.createQuery(Profile.Trainer.class);
        Root<Trainer> root = cq.from(Trainer.class);

        cq.select(cb.construct(
                Profile.Trainer.class,
                root.get("firstName"),
                root.get("lastName"),
                root.get("username"),
                root.get("specialization").get("trainingTypeName")
        ));

        if (!assignedTrainers.isEmpty()) {
            cq.where(cb.not(root.in(assignedTrainers)));
        }

        return em.createQuery(cq).getResultList();
    }

    @Override
    public List<Profile.Trainer> findAssignedTrainers(String username) {
        return em.createNamedQuery(
                        "Trainee.findWithTrainersByUsername",
                        Trainee.class)
                .setParameter("username", username)
                .getSingleResult()
                .getTrainers()
                .stream()
                .map(Profile.Trainer::from)
                .toList();
    }
}
