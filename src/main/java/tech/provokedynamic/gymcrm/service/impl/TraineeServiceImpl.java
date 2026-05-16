package tech.provokedynamic.gymcrm.service.impl;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.provokedynamic.gymcrm.annotation.Authenticated;
import tech.provokedynamic.gymcrm.annotation.Validate;
import tech.provokedynamic.gymcrm.dao.TraineeDao;
import tech.provokedynamic.gymcrm.dao.TrainerDao;
import tech.provokedynamic.gymcrm.dto.Profile;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.dto.Summary;
import tech.provokedynamic.gymcrm.entity.Trainee;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.exception.AlreadyActivatedException;
import tech.provokedynamic.gymcrm.exception.AlreadyDeactivatedException;
import tech.provokedynamic.gymcrm.exception.UserDoesNotExistException;
import tech.provokedynamic.gymcrm.service.TraineeService;
import tech.provokedynamic.gymcrm.util.CredentialGenerator;

import java.time.LocalDate;
import java.util.List;

@Service
public class TraineeServiceImpl implements TraineeService {

    private static final Logger log = LoggerFactory.getLogger(TraineeServiceImpl.class);

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final CredentialGenerator credentialGenerator;

    public TraineeServiceImpl(
            TraineeDao traineeDao,
            TrainerDao trainerDao,
            CredentialGenerator credentialGenerator
    ) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.credentialGenerator = credentialGenerator;
    }

    @Override
    @Validate
    @Transactional
    public Profile.Trainee create(Request.CreateTrainee request) {
        var username = credentialGenerator.generateUsername(request.firstName(), request.lastName());
        var password = credentialGenerator.generatePassword();

        var trainee = Trainee.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .username(username)
                .password(password)
                .dateOfBirth(request.dateOfBirth())
                .address(request.address())
                .build();

        traineeDao.save(trainee);

        log.info("Created trainee profile for username '{}'", username);

        return Profile.Trainee.from(trainee);
    }

    @Override
    @Transactional(readOnly = true)
    public Profile.Trainee getProfile(String username) {
        log.debug("Fetching profile for trainee '{}'", username);
        return traineeDao.findByUsername(username)
                .map(Profile.Trainee::from)
                .orElseThrow(() -> new UserDoesNotExistException(username));
    }

    @Override
    @Validate
    @Authenticated
    @Transactional
    public void changePassword(Request.ChangePassword request) {
        log.info("Changing password for trainee '{}'", request.username());
        traineeDao.updatePassword(request.username(), request.newPassword());
    }

    @Override
    @Validate
    @Authenticated
    @Transactional
    public Profile.Trainee update(Request.UpdateTrainee request) {
        Trainee trainee = traineeDao.findByUsername(request.username())
                .orElseThrow(() -> new UserDoesNotExistException(request.username()));

        Trainee updated = trainee.toBuilder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .dateOfBirth(request.dateOfBirth())
                .address(request.address())
                .build();

        traineeDao.update(updated);

        log.info("Updated trainee profile for username '{}'", request.username());

        return Profile.Trainee.from(updated);
    }

    @Override
    @Validate
    @Authenticated
    @Transactional
    public void activate(Request.ToggleActive request) {
        String username = request.username();

        if (traineeDao.activateByUsername(username) == 0) {
            log.warn("Trainee '{}' is already active", username);
            throw new AlreadyActivatedException(username);
        }
        log.info("Activated trainee '{}'", username);
    }

    @Override
    @Validate
    @Authenticated
    @Transactional
    public void deactivate(Request.ToggleActive request) {
        String username = request.username();

        if (traineeDao.deactivateByUsername(username) == 0) {
            log.warn("Trainee '{}' is already inactive", username);
            throw new AlreadyDeactivatedException(username);
        }
        log.info("Deactivated trainee '{}'", username);
    }

    @Override
    @Validate
    @Authenticated
    @Transactional
    public void delete(Request.DeleteTrainee request) {
        String username = request.username();

        Trainee trainee = traineeDao.findByUsername(username)
                .orElseThrow(() -> new UserDoesNotExistException(username));

        traineeDao.delete(trainee);

        log.info("Deleted trainee '{}'", username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Summary.Training> getTrainings(
            String username,
            @Nullable LocalDate from,
            @Nullable LocalDate to,
            @Nullable String trainerUsername,
            @Nullable String trainingType
    ) {
        log.debug("Fetching trainings for trainee '{}'", username);
        return traineeDao.findTrainingsByUsername(username, from, to, trainerUsername, trainingType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Profile.Trainer> getUnassignedTrainers(String username) {
        log.debug("Fetching unassigned trainers for trainee '{}'", username);
        return traineeDao.findUnassignedTrainers(username);
    }

    @Override
    @Validate
    @Authenticated
    @Transactional
    public List<Profile.Trainer> updateTrainers(Request.UpdateTraineeTrainers request) {
        Trainee trainee = traineeDao.findByUsername(request.username())
                .orElseThrow(() -> new UserDoesNotExistException(request.username()));

        List<Trainer> newTrainers = trainerDao.findByUsernames(request.trainerUsernames());

        if (newTrainers.size() != request.trainerUsernames().size()) {
            throw new UserDoesNotExistException("One or more trainer usernames not found");
        }

        trainee.getTrainers().clear();
        trainee.getTrainers().addAll(newTrainers);
        traineeDao.update(trainee);

        log.info("Updated trainers list for trainee '{}'", request.username());
        return traineeDao.findAssignedTrainers(request.username());
    }
}
