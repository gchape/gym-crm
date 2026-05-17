package tech.provokedynamic.gymcrm.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.postgresql.PostgreSQLContainer;

@ExtendWith(SpringExtension.class)
@Transactional
public abstract class BaseDaoTest {

    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:18.3").withReuse(true);

    @PersistenceContext
    protected EntityManager em;

    @BeforeAll
    static void startContainer() {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("db.url", POSTGRES::getJdbcUrl);
        registry.add("db.user", POSTGRES::getUsername);
        registry.add("db.password", POSTGRES::getPassword);
    }

    @Configuration
    static class BaseConfig {

        @Bean
        public JpaTransactionManager transactionManager(EntityManagerFactory emf) {
            return new JpaTransactionManager(emf);
        }
    }
}
