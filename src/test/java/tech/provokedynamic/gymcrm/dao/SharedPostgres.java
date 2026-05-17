package tech.provokedynamic.gymcrm.dao;

import org.testcontainers.postgresql.PostgreSQLContainer;

public final class SharedPostgres {

    static final PostgreSQLContainer INSTANCE =
            new PostgreSQLContainer("postgres:18.3");

    static {
        INSTANCE.start();
        Runtime.getRuntime().addShutdownHook(
                new Thread(INSTANCE::stop, "testcontainers-shutdown")
        );
    }

    private SharedPostgres() {
    }
}
