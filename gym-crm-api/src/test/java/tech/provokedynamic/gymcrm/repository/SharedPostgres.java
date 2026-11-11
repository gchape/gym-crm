package tech.provokedynamic.gymcrm.repository;

import org.testcontainers.postgresql.PostgreSQLContainer;

public final class SharedPostgres {

    static final PostgreSQLContainer INSTANCE =
            new PostgreSQLContainer("postgres:18.4");

    static {
        INSTANCE.start();
        Runtime.getRuntime().addShutdownHook(
                new Thread(INSTANCE::stop, "testcontainers-shutdown")
        );
    }

    private SharedPostgres() {
    }
}
