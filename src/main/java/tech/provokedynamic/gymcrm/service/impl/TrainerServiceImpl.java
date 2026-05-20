package tech.provokedynamic.gymcrm.service.impl;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.provokedynamic.gymcrm.annotation.Authenticated;
import tech.provokedynamic.gymcrm.annotation.Validate;
import tech.provokedynamic.gymcrm.dto.Profile;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.dto.Summary;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.exception.AlreadyActivatedException;
import tech.provokedynamic.gymcrm.exception.AlreadyDeactivatedException;
import tech.provokedynamic.gymcrm.exception.TrainingTypeNotFoundException;
import tech.provokedynamic.gymcrm.exception.UserDoesNotExistException;
import tech.provokedynamic.gymcrm.repository.TrainerRepository;
import tech.provokedynamic.gymcrm.repository.TrainingTypeRepository;
import tech.provokedynamic.gymcrm.service.TrainerService;
import tech.provokedynamic.gymcrm.util.CredentialGenerator;

import java.time.LocalDate;
import java.util.List;

@Service
public class TrainerServiceImpl implements TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerServiceImpl.class);

    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    
    private final CredentialGenerator credentialGenerator;

    public TrainerServiceImpl(
            TrainerRepository trainerRepository,
            TrainingTypeRepository trainingTypeRepository,
            CredentialGenerator credentialGenerator
    ) {
        this.trainerRepository = trainerRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.credentialGenerator = credentialGenerator;
    }

    @Override
    @Validate
    @Transactional
    public Profile.Trainer create(Request.CreateTrainer request) {
        var username = credentialGenerator.generateUsername(request.firstName(), request.lastName());
        var password = credentialGenerator.generatePassword();

        var specialization = trainingTypeRepository.findByTrainingTypeName(request.specialization())
                .orElseThrow(() -> new TrainingTypeNotFoundException(request.specialization()));

        var trainer = Trainer.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .username(username)
                .password(password)
                .specialization(specialization)
                .build();

        trainerRepository.save(trainer);

        log.info("Created trainer profile for username '{}'", username);

        return Profile.Trainer.from(trainer);
    }

    @Override
    @Transactional(readOnly = true)
    public Profile.Trainer getProfile(String username) {
        log.debug("Fetching profile for trainer '{}'", username);

        var profile = trainerRepository.findByUsername(username)
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

        var trainee = trainerRepository.findByUsernameAndPassword(request.username(), request.password());

        if (trainee.isPresent()) {
            var updated = trainee.map(Trainer::toBuilder)
                    .get()
                    .password(request.newPassword())
                    .build();

            trainerRepository.save(updated);
        } else throw new UserDoesNotExistException(request.username());

        log.debug("Password changed for trainer '{}'", request.username());
    }

    @Override
    @Validate
    @Authenticated
    @Transactional
    public Profile.Trainer update(Request.UpdateTrainer request) {
        log.debug("Updating trainer profile for '{}'", request.username());

        var trainer = trainerRepository.findByUsername(request.username())
                .orElseThrow(() -> new UserDoesNotExistException(request.username()));

        var specialization = trainingTypeRepository.findByTrainingTypeName(request.specialization())
                .orElseThrow(() -> new TrainingTypeNotFoundException(request.specialization()));

        var updated = trainer.toBuilder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .specialization(specialization)
                .build();

        trainerRepository.save(updated);

        log.info("Updated trainer profile for username '{}'", request.username());

        return Profile.Trainer.from(updated);
    }

    @Override
    @Validate
    @Authenticated
    @Transactional
    public void activate(Request.ToggleActive request) {
        String username = request.username();

        if (trainerRepository.activateByUsername(username) == 0) {
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

        if (trainerRepository.deactivateByUsername(username) == 0) {
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

        var trainings = trainerRepository.findTrainingsByUsername(username, from, to, traineeUsername);

        log.debug("Found {} trainings for trainer '{}'", trainings.size(), username);

        return trainings;
    }
}
