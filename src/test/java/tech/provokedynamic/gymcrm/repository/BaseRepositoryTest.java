package tech.provokedynamic.gymcrm.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@ExtendWith(SpringExtension.class)
public abstract class BaseRepositoryTest {

    @PersistenceContext
    protected EntityManager em;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", SharedPostgres.INSTANCE::getJdbcUrl);
        registry.add("spring.datasource.username", SharedPostgres.INSTANCE::getUsername);
        registry.add("spring.datasource.password", SharedPostgres.INSTANCE::getPassword);
    }
}