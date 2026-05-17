package tech.provokedynamic.gymcrm.dao.impl;

import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import org.hibernate.jpa.AvailableHints;
import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.dao.TrainerDao;
import tech.provokedynamic.gymcrm.dto.Summary;
import tech.provokedynamic.gymcrm.entity.Trainee;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.entity.Training;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TrainerDaoImpl extends UserDaoImpl implements TrainerDao {

    public TrainerDaoImpl(EntityManager em) {
        super(em);
    }

    @Override
    public void save(Trainer trainer) {
        em.persist(trainer);
    }

    @Override
    public void update(Trainer trainer) {
        em.merge(trainer);
    }

    @Override
    public boolean existsByUsername(String username) {
        return em.createQuery(
                        "SELECT count(tr.id) > 0 FROM Trainer tr WHERE tr.username = :username",
                        Boolean.class)
                .setParameter("username", username)
                .setHint(AvailableHints.HINT_READ_ONLY, true)
                .getSingleResult();
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        try {
            return Optional.of(
                    em.createQuery(
                                    "FROM Trainer tr WHERE tr.username = :username",
                                    Trainer.class)
                            .setParameter("username", username)
                            .getSingleResult()
            );
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Trainer> findByUsernames(List<String> usernames) {
        return em.createQuery(
                        "FROM Trainer tr WHERE tr.username IN :usernames",
                        Trainer.class)
                .setParameter("usernames", usernames)
                .getResultList();
    }

    @Override
    public List<Summary.Training> findTrainingsByUsername(
            String username,
            @Nullable LocalDate from,
            @Nullable LocalDate to,
            @Nullable String trainee
    ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Summary.Training> cq = cb.createQuery(Summary.Training.class);
        Root<Training> root = cq.from(Training.class);

        Join<Training, Trainer> trainerJoin = root.join("trainer");
        Join<Training, Trainee> traineeJoin = root.join("trainee");

        cq.select(cb.construct(Summary.Training.class,
                root.get("trainingName"),
                root.get("trainingDate"),
                root.get("trainingDuration"),
                traineeJoin.get("username")
        ));

        List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(trainerJoin.get("username"), username));

        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("trainingDate"), from));
        }
        if (to != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("trainingDate"), to));
        }
        if (trainee != null) {
            predicates.add(cb.equal(traineeJoin.get("username"), trainee));
        }

        cq.where(predicates);
        cq.orderBy(cb.desc(root.get("trainingDate")));

        return em.createQuery(cq).getResultList();
    }
}
