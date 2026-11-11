-- Keep existing catalog entries when upgrading an existing database.
INSERT INTO training_type (training_type_name)
VALUES ('CARDIO'), ('STRENGTH'), ('FLEXIBILITY'), ('HIIT')
ON CONFLICT (training_type_name) DO NOTHING;

CREATE INDEX idx_training_trainee_date ON training (trainee_id, training_date DESC, id DESC);
CREATE INDEX idx_training_trainer_date ON training (trainer_id, training_date DESC, id DESC);
CREATE INDEX idx_trainee_trainer_trainer ON trainee_trainer (trainer_id);
