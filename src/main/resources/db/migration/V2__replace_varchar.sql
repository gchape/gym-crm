-- user
ALTER TABLE "user"
ALTER
COLUMN u_type TYPE TEXT,
    ALTER
COLUMN first_name TYPE TEXT,
    ALTER
COLUMN last_name TYPE TEXT,
    ALTER
COLUMN username TYPE TEXT,
    ALTER
COLUMN password TYPE TEXT;

ALTER TABLE "user"
    ADD CONSTRAINT chk_user_u_type_length
        CHECK (char_length(u_type) <= 31),
    ADD CONSTRAINT chk_user_first_name_length
        CHECK (char_length(first_name) <= 255),
    ADD CONSTRAINT chk_user_last_name_length
        CHECK (char_length(last_name) <= 255),
    ADD CONSTRAINT chk_user_username_length
        CHECK (char_length(username) <= 255),
    ADD CONSTRAINT chk_user_password_length
        CHECK (char_length(password) <= 255);

-- training_type
ALTER TABLE training_type
ALTER
COLUMN training_type_name TYPE TEXT;

ALTER TABLE training_type
    ADD CONSTRAINT chk_training_type_name_length
        CHECK (char_length(training_type_name) <= 255);

-- trainee
ALTER TABLE trainee
ALTER
COLUMN street TYPE TEXT,
    ALTER
COLUMN city TYPE TEXT,
    ALTER
COLUMN zip TYPE TEXT,
    ALTER
COLUMN country TYPE TEXT;

ALTER TABLE trainee
    ADD CONSTRAINT chk_trainee_street_length
        CHECK (street IS NULL OR char_length(street) <= 255),
    ADD CONSTRAINT chk_trainee_city_length
        CHECK (city IS NULL OR char_length(city) <= 255),
    ADD CONSTRAINT chk_trainee_zip_length
        CHECK (zip IS NULL OR char_length(zip) <= 50),
    ADD CONSTRAINT chk_trainee_country_length
        CHECK (country IS NULL OR char_length(country) <= 100);

-- training
ALTER TABLE training
ALTER
COLUMN training_name TYPE TEXT;

ALTER TABLE training
    ADD CONSTRAINT chk_training_name_length
        CHECK (char_length(training_name) <= 255);