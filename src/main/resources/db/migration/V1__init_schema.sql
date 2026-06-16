-- V1__init_schema.sql

-- Sequences
CREATE SEQUENCE IF NOT EXISTS user_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS training_seq START WITH 1 INCREMENT BY 50;

-- training_type (referenced by trainer and training; uses IDENTITY, no sequence)
CREATE TABLE training_type
(
    id                 BIGSERIAL PRIMARY KEY,
    training_type_name VARCHAR(255) NOT NULL UNIQUE
);

-- user (base table; JOINED inheritance; soft-delete via is_active)
CREATE TABLE "user"
(
    id         BIGINT PRIMARY KEY    DEFAULT nextval('user_seq'),
    u_type     VARCHAR(31)  NOT NULL,             -- discriminator column
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    username   VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE -- SoftDelete(strategy = ACTIVE)
);

-- trainee (joined sub-table; ON DELETE CASCADE mirrors @OnDelete(CASCADE))
CREATE TABLE trainee
(
    id      BIGINT PRIMARY KEY REFERENCES "user" (id) ON DELETE CASCADE,
    dob     DATE, -- @Nullable @Column(name = "dob")
    -- Address @Embedded — adjust column names to match your Address record/class
    street  VARCHAR(255),
    city    VARCHAR(255),
    zip     VARCHAR(50),
    country VARCHAR(100)
);

-- trainer (joined sub-table)
CREATE TABLE trainer
(
    id               BIGINT PRIMARY KEY REFERENCES "user" (id) ON DELETE CASCADE,
    training_type_id BIGINT NOT NULL REFERENCES training_type (id)
);

-- training
CREATE TABLE training
(
    id                BIGINT PRIMARY KEY DEFAULT nextval('training_seq'),
    training_name     VARCHAR(255) NOT NULL,
    training_date     DATE         NOT NULL,
    training_duration INTEGER      NOT NULL,
    trainee_id        BIGINT       NOT NULL REFERENCES trainee (id),
    trainer_id        BIGINT       NOT NULL REFERENCES trainer (id),
    training_type_id  BIGINT       NOT NULL REFERENCES training_type (id)
);

-- trainee_trainer (ManyToMany join table)
CREATE TABLE trainee_trainer
(
    trainee_id BIGINT NOT NULL REFERENCES trainee (id),
    trainer_id BIGINT NOT NULL REFERENCES trainer (id),
    PRIMARY KEY (trainee_id, trainer_id)
);