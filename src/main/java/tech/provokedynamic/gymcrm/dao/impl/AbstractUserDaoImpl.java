package tech.provokedynamic.gymcrm.dao.impl;

public class AbstractDaoImpl<T> {
    protected AbstractEntityDaoImpl(EntityManager em) {
        super(em);
    }

    protected void persist(T entity) {
        em.persist(entity);
    }

    protected void merge(T entity) {
        em.merge(entity);
    }

    // Shared criteria builder for training summaries
    protected List<Summary.Training> findTrainings(
            String filterField,   // "trainee" or "trainer"
            String username,
            String partnerField,  // "trainer" or "trainee"
            LocalDate from,
            LocalDate to,
            String partnerUsername,
            String type           // only used by trainee-side; pass null for trainer-side
    ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Summary.Training> cq = cb.createQuery(Summary.Training.class);
        Root<Training> root = cq.from(Training.class);

        Join<Training, ?> filterJoin = root.join(filterField);
        Join<Training, ?> partnerJoin = root.join(partnerField);

        cq.select(cb.construct(Summary.Training.class,
                root.get("trainingName"),
                root.get("trainingDate"),
                root.get("trainingDuration"),
                partnerJoin.get("username")
        ));

        List<Predicate> predicates = new java.util.ArrayList<>();
        predicates.add(cb.equal(filterJoin.get("username"), username));

        if (from != null)
            predicates.add(cb.greaterThanOrEqualTo(root.get("trainingDate"), from));
        if (to != null)
            predicates.add(cb.lessThanOrEqualTo(root.get("trainingDate"), to));
        if (partnerUsername != null)
            predicates.add(cb.equal(partnerJoin.get("username"), partnerUsername));
        if (type != null) {
            Join<Training, ?> typeJoin = root.join("trainingType");
            predicates.add(cb.equal(typeJoin.get("trainingTypeName"), type));
        }

        cq.where(predicates);
        cq.orderBy(cb.desc(root.get("trainingDate")));
        return em.createQuery(cq).getResultList();
    }
}
