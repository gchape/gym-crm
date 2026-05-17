package tech.provokedynamic.gymcrm.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import org.hibernate.jpa.AvailableHints;
import org.jspecify.annotations.Nullable;
import tech.provokedynamic.gymcrm.dao.UserDao;
import tech.provokedynamic.gymcrm.dto.Summary;
import tech.provokedynamic.gymcrm.entity.*;
import tech.provokedynamic.gymcrm.model.UserType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractUserDaoImpl implements UserDao {

    private static final String EXISTS_BY_USERNAME_INCLUDING_DELETED =
            "SELECT count(*) > 0 FROM \"user\" WHERE username = :username";

    private static final String EXISTS_BY_USERNAME_AND_PASSWORD =
            "SELECT count(u.id) > 0 FROM User u WHERE u.username = :username AND u.password = :password";

    private static final String UPDATE_PASSWORD =
            "UPDATE \"user\" SET password = :newPassword WHERE username = :username";

    private static final String SET_ACTIVE_STATUS =
            "UPDATE \"user\" SET is_active = :active WHERE username = :username AND is_active = :current";

    protected final EntityManager em;

    protected AbstractUserDaoImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(User user) {
        em.persist(user);
    }

    @Override
    public void update(User user) {
        em.merge(user);
    }

    @Override
    public boolean existsByUsernameIncludingDeleted(String username) {
        return (boolean) em.createNativeQuery(EXISTS_BY_USERNAME_INCLUDING_DELETED)
                .setParameter("username", username)
                .setHint(AvailableHints.HINT_READ_ONLY, true)
                .getSingleResult();
    }

    @Override
    public boolean existsByUsernameAndPassword(String username, String password) {
        return em.createQuery(EXISTS_BY_USERNAME_AND_PASSWORD, Boolean.class)
                .setParameter("username", username)
                .setParameter("password", password)
                .setHint(AvailableHints.HINT_READ_ONLY, true)
                .getSingleResult();
    }

    @Override
    public void updatePassword(String username, String newPassword) {
        em.createNativeQuery(UPDATE_PASSWORD)
                .setParameter("newPassword", newPassword)
                .setParameter("username", username)
                .setHint(AvailableHints.HINT_NATIVE_SPACES, User.class)
                .executeUpdate();
    }

    @Override
    public int deactivateByUsername(String username) {
        return setActiveStatus(username, false);
    }

    @Override
    public int activateByUsername(String username) {
        return setActiveStatus(username, true);
    }

    private int setActiveStatus(String username, boolean active) {
        return em.createNativeQuery(SET_ACTIVE_STATUS)
                .setParameter("active", active)
                .setParameter("username", username)
                .setParameter("current", !active)
                .setHint(AvailableHints.HINT_NATIVE_SPACES, User.class)
                .executeUpdate();
    }

    protected List<Summary.Training> findTrainingsByUsername(
            String username,
            UserType userType,
            @Nullable LocalDate from,
            @Nullable LocalDate to,
            @Nullable String filterUsername,
            @Nullable String type
    ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Summary.Training> cq = cb.createQuery(Summary.Training.class);
        Root<Training> root = cq.from(Training.class);

        Join<Training, Trainer> trainerJoin = root.join(Training_.trainer);
        Join<Training, Trainee> traineeJoin = root.join(Training_.trainee);

        var partnerSelection = userType == UserType.TRAINEE
                ? trainerJoin.get(Trainer_.username)
                : traineeJoin.get(Trainee_.username);

        cq.select(cb.construct(Summary.Training.class,
                root.get(Training_.trainingName),
                root.get(Training_.trainingDate),
                root.get(Training_.trainingDuration),
                partnerSelection
        ));

        List<Predicate> predicates = new ArrayList<>();
        switch (userType) {
            case TRAINEE -> predicates.add(cb.equal(traineeJoin.get(Trainee_.username), username));
            case TRAINER -> predicates.add(cb.equal(trainerJoin.get(Trainer_.username), username));
        }

        if (from != null)
            predicates.add(cb.greaterThanOrEqualTo(root.get(Training_.trainingDate), from));
        if (to != null)
            predicates.add(cb.lessThanOrEqualTo(root.get(Training_.trainingDate), to));
        if (filterUsername != null)
            switch (userType) {
                case TRAINER -> predicates.add(cb.equal(traineeJoin.get(Trainee_.username), filterUsername));
                case TRAINEE -> predicates.add(cb.equal(trainerJoin.get(Trainer_.username), filterUsername));
            }
        if (type != null) {
            Join<Training, TrainingType> typeJoin = root.join(Training_.trainingType);
            predicates.add(cb.equal(typeJoin.get(TrainingType_.trainingTypeName), type));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.desc(root.get(Training_.trainingDate)));
        return em.createQuery(cq).getResultList();
    }
}
