package tech.provokedynamic.gymcrm.service.impl;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.provokedynamic.gymcrm.annotation.Authenticated;
import tech.provokedynamic.gymcrm.annotation.Validate;
import tech.provokedynamic.gymcrm.dao.TrainerDao;
import tech.provokedynamic.gymcrm.dao.TrainingTypeDao;
import tech.provokedynamic.gymcrm.dto.Profile;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.dto.Summary;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.exception.AlreadyActivatedException;
import tech.provokedynamic.gymcrm.exception.AlreadyDeactivatedException;
import tech.provokedynamic.gymcrm.exception.UserDoesNotExistException;
import tech.provokedynamic.gymcrm.service.TrainerService;
import tech.provokedynamic.gymcrm.util.CredentialGenerator;

import java.time.LocalDate;
import java.util.List;

@Service
public class TrainerServiceImpl implements TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerServiceImpl.class);

    private final TrainerDao trainerDao;
    private final TrainingTypeDao trainingTypeDao;
    private final CredentialGenerator credentialGenerator;

    public TrainerServiceImpl(
            TrainerDao trainerDao,
            TrainingTypeDao trainingTypeDao,
            CredentialGenerator credentialGenerator
    ) {
        this.trainerDao = trainerDao;
        this.trainingTypeDao = trainingTypeDao;
        this.credentialGenerator = credentialGenerator;
    }

    @Override
    @Validate
    @Transactional
    public Profile.Trainer create(Request.CreateTrainer request) {
        var username = credentialGenerator.generateUsername(request.firstName(), request.lastName());
        var password = credentialGenerator.generatePassword();
        var specialization = trainingTypeDao.findByName(request.specialization());

        var trainer = Trainer.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .username(username)
                .password(password)
                .specialization(specialization)
                .build();

        trainerDao.save(trainer);

        log.info("Created trainer profile for username '{}'", username);

        return Profile.Trainer.from(trainer);
    }

    @Override
    @Transactional(readOnly = true)
    public Profile.Trainer getProfile(String username) {
        log.debug("Fetching profile for trainer '{}'", username);

        var profile = trainerDao.findByUsername(username)
                .map(Profile.Trainer::from)
                .orElseThrow(() -> new UserDoesNotExistException(username));

        log.debug("Fetched profile for trainer '{}'", username);

        return profile;
    }

    @Override
    @Validate
    @Authenticated
    @Transactional
    public void changePassword(Request.ChangePassword request) {
        log.debug("Changing password for trainer '{}'", request.username());

        trainerDao.updatePassword(request.username(), request.newPassword());

        log.debug("Password changed for trainer '{}'", request.username());
    }

    @Override
    @Validate
    @Authenticated
    @Transactional
    public Profile.Trainer update(Request.UpdateTrainer request) {
        log.debug("Updating trainer profile for '{}'", request.username());

        Trainer trainer = trainerDao.findByUsername(request.username())
                .orElseThrow(() -> new UserDoesNotExistException(request.username()));

        var specialization = trainingTypeDao.findByName(request.specialization());

        Trainer updated = trainer.toBuilder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .specialization(specialization)
                .build();

        trainerDao.update(updated);

        log.info("Updated trainer profile for username '{}'", request.username());

        return Profile.Trainer.from(updated);
    }

    @Override
    @Validate
    @Authenticated
    @Transactional
    public void activate(Request.ToggleActive request) {
        String username = request.username();

        if (trainerDao.activateByUsername(username) == 0) {
            log.warn("Trainer '{}' is already active", username);
            throw new AlreadyActivatedException(username);
        }

        log.info("Activated trainer '{}'", username);
    }

    @Override
    @Validate
    @Authenticated
    @Transactional
    public void deactivate(Request.ToggleActive request) {
        String username = request.username();

        if (trainerDao.deactivateByUsername(username) == 0) {
            log.warn("Trainer '{}' is already inactive", username);
            throw new AlreadyDeactivatedException(username);
        }

        log.info("Deactivated trainer '{}'", username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Summary.Training> getTrainings(
            String username,
            @Nullable LocalDate from,
            @Nullable LocalDate to,
            @Nullable String traineeUsername
    ) {
        log.debug("Fetching trainings for trainer '{}' [from={}, to={}, trainee={}]",
                username, from, to, traineeUsername);
        var trainings = trainerDao.findTrainingsByUsername(username, from, to, traineeUsername);
        log.debug("Found {} trainings for trainer '{}'", trainings.size(), username);
        return trainings;
    }
}
