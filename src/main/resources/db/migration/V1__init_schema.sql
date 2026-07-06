-- V1__init_schema.sql

CREATE TABLE "user"
(
    id         BIGSERIAL PRIMARY KEY,
    u_type     VARCHAR(31)  NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    username   VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE trainee
(
    id          BIGINT PRIMARY KEY REFERENCES "user" (id) ON DELETE CASCADE,
    dob         DATE,
    street      VARCHAR(255),
    city        VARCHAR(255),
    country     VARCHAR(255),
    postal_code VARCHAR(10)
);

CREATE TABLE training_type
(
    id                 BIGSERIAL PRIMARY KEY,
    training_type_name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE trainer
(
    id               BIGINT PRIMARY KEY REFERENCES "user" (id) ON DELETE CASCADE,
    training_type_id BIGINT NOT NULL REFERENCES training_type (id)
);

CREATE TABLE training
(
    id                BIGSERIAL PRIMARY KEY,
    training_name     VARCHAR(255) NOT NULL,
    training_date     DATE         NOT NULL,
    training_duration INTEGER      NOT NULL,
    trainee_id        BIGINT       NOT NULL REFERENCES trainee (id),
    trainer_id        BIGINT       NOT NULL REFERENCES trainer (id),
    training_type_id  BIGINT       NOT NULL REFERENCES training_type (id)
);

CREATE TABLE trainee_trainer
(
    trainee_id BIGINT NOT NULL REFERENCES trainee (id),
    trainer_id BIGINT NOT NULL REFERENCES trainer (id),
    PRIMARY KEY (trainee_id, trainer_id)
);

CREATE SEQUENCE user_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE training_seq START WITH 1 INCREMENT BY 50;
