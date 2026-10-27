package tech.provokedynamic.gymcrm.cucumber;

import io.cucumber.java.Before;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class Hooks {

    @Autowired
    private EntityManager em;

    @Before
    @Transactional
    public void resetState() {
        // Physically purge rather than using the soft-delete-aware
        // repository deleteAll(), which only flips is_active and leaves
        // the username "taken" for DBCredentialGenerator across scenarios.
        em.createNativeQuery("DELETE FROM training").executeUpdate();
        em.createNativeQuery("DELETE FROM trainee_trainer").executeUpdate();
        em.createNativeQuery("DELETE FROM trainer").executeUpdate();
        em.createNativeQuery("DELETE FROM trainee").executeUpdate();
        em.createNativeQuery("DELETE FROM \"user\"").executeUpdate();
    }
}
