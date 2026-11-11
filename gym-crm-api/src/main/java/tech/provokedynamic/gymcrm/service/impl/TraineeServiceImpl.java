package tech.provokedynamic.gymcrm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.provokedynamic.gymcrm.annotation.Validate;
import tech.provokedynamic.gymcrm.client.WorkloadEventPublisher;
import tech.provokedynamic.gymcrm.dto.Profile;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.dto.Response;
import tech.provokedynamic.gymcrm.dto.Summary;
import tech.provokedynamic.gymcrm.entity.Trainee;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.entity.Training;
import tech.provokedynamic.gymcrm.entity.User;
import tech.provokedynamic.gymcrm.exception.AlreadyActivatedException;
import tech.provokedynamic.gymcrm.exception.AlreadyDeactivatedException;
import tech.provokedynamic.gymcrm.exception.AuthenticationException;
import tech.provokedynamic.gymcrm.exception.UserDoesNotExistException;
import tech.provokedynamic.gymcrm.repository.TraineeRepository;
import tech.provokedynamic.gymcrm.repository.TrainerRepository;
import tech.provokedynamic.gymcrm.repository.TrainingRepository;
import tech.provokedynamic.gymcrm.service.TraineeService;
import tech.provokedynamic.gymcrm.util.CredentialGenerator;
import tech.provokedynamic.gymcrmcommon.event.WorkloadEvent;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TraineeServiceImpl implements TraineeService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;

    private final CredentialGenerator credentialGenerator;
    private final PasswordEncoder passwordEncoder;

    private final WorkloadEventPublisher workloadEventPublisher;

    @Override
    @Validate
    @Transactional
    public Response.CreatedUser create(Request.CreateTrainee request) {
        var username = credentialGenerator.generateUsername(request.firstName(), request.lastName());
        var rawPassword = credentialGenerator.generatePassword();

        var trainee = Trainee.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .dateOfBirth(request.dateOfBirth())
                .address(request.address())
                .build();

        traineeRepository.save(trainee);

        log.info("Created trainee profile for username '{}'", username);

        return new Response.CreatedUser(username, rawPassword);
    }

    @Override
    @Transactional(readOnly = true)
    public Profile.Trainee getProfile(String username) {
        log.debug("Fetching profile for trainee '{}'", username);

        var profile = traineeRepository.findByUsername(username)
                .map(Profile.Trainee::from)
                .orElseThrow(() -> new UserDoesNotExistException(username));

        log.debug("Fetched profile for trainee '{}'", username);

        return profile;
    }

    @Override
    @Validate
    @Transactional
    public void changePassword(Request.ChangePassword request) {
        log.debug("Changing password for trainee '{}'", request.username());

        var trainee = traineeRepository.findByUsername(request.username())
                .orElseThrow(() -> new UserDoesNotExistException(request.username()));

        if (!passwordEncoder.matches(request.password(), trainee.getPassword())) {
            log.warn("Password change rejected for trainee '{}': current password mismatch", request.username());
            throw new AuthenticationException("Invalid current password");
        }

        var updated = trainee.toBuilder()
                .password(passwordEncoder.encode(request.newPassword()))
                .build();

        traineeRepository.save(updated);

        log.debug("Password changed for trainee '{}'", request.username());
    }

    @Override
    @Validate
    @Transactional
    public Profile.Trainee update(Request.UpdateTrainee request) {
        log.debug("Updating trainee profile for '{}'", request.username());

        var trainee = traineeRepository.findByUsername(request.username())
                .orElseThrow(() -> new UserDoesNotExistException(request.username()));

        var updated = trainee.toBuilder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .dateOfBirth(request.dateOfBirth())
                .address(request.address())
                .build();

        traineeRepository.save(updated);

        log.info("Updated trainee profile for username '{}'", request.username());

        return Profile.Trainee.from(updated);
    }

    @Override
    @Validate
    @Transactional
    public void activate(Request.ToggleActive request) {
        String username = request.username();

        log.debug("Activating trainee '{}'", username);

        if (traineeRepository.activateByUsername(username) == 0) {
            log.warn("Trainee '{}' is already active", username);
            throw new AlreadyActivatedException(username);
        }

        log.info("Activated trainee '{}'", username);
    }

    @Override
    @Validate
    @Transactional
    public void deactivate(Request.ToggleActive request) {
        String username = request.username();

        log.debug("Deactivating trainee '{}'", username);

        if (traineeRepository.deactivateByUsername(username) == 0) {
            log.warn("Trainee '{}' is already inactive", username);
            throw new AlreadyDeactivatedException(username);
        }

        log.info("Deactivated trainee '{}'", username);
    }

    @Override
    @Validate
    @Transactional
    public void delete(Request.DeleteTrainee request) {
        String username = request.username();

        log.debug("Deleting trainee '{}'", username);

        var trainings = trainingRepository.findAllByTraineeUsernameWithTrainer(username);

        traineeRepository.deleteByUsername(username);

        // Notify gym-crm-workload only after this transaction actually commits.
        publishDeletionsAfterCommit(trainings);

        log.info("Deleted trainee '{}'", username);
    }

    private void publishDeletionsAfterCommit(List<Training> trainings) {
        // Capture the fields we need now — the entities/session won't be
        // available anymore once the transaction has committed.
        var events = trainings.stream()
                .map(training -> {
                    var trainer = training.getTrainer();
                    return new WorkloadEvent(
                            trainer.getUsername(), trainer.getFirstName(), trainer.getLastName(),
                            trainer.isActive(), training.getTrainingDate(), training.getTrainingDuration(),
                            WorkloadEvent.ActionType.DELETE
                    );
                })
                .toList();

        workloadEventPublisher.publishAfterCommit(events);
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
        log.debug("Fetching trainings for trainee '{}' [from={}, to={}, trainer={}, type={}]",
                username, from, to, trainerUsername, trainingType);

        var trainings = traineeRepository.findTrainingsByUsername(username, from, to, trainerUsername, trainingType);

        log.debug("Found {} trainings for trainee '{}'", trainings.size(), username);

        return trainings;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Profile.Trainer> getUnassignedTrainers(String username) {
        log.debug("Fetching unassigned trainers for trainee '{}'", username);

        var trainee = traineeRepository.findWTrainersByUsername(username)
                .orElseThrow(() -> new UserDoesNotExistException(username));

        var ids = trainee.getTrainers().stream()
                .map(Trainer::getId)
                .collect(Collectors.toSet());

        var trainers = trainerRepository.findAllByIdNotIn(ids);

        log.debug("Found {} unassigned trainers for trainee '{}'", trainers.size(), username);

        return trainers.stream().map(Profile.Trainer::from).toList();
    }

    @Override
    @Validate
    @Transactional
    public List<Profile.Trainer> updateTrainers(Request.UpdateTraineeTrainers request) {
        log.debug("Updating trainers for trainee '{}'", request.username());

        var trainee = traineeRepository.findByUsername(request.username())
                .orElseThrow(() -> new UserDoesNotExistException(request.username()));

        var usernames = request.trainerUsernames();
        var newTrainers = trainerRepository.findAllByUsernameIn(usernames);

        if (newTrainers.size() != usernames.size()) {
            log.warn("Some trainer usernames not found, requested={}, found={}",
                    usernames, newTrainers.stream().map(User::getUsername).toList());
            throw new UserDoesNotExistException("One or more trainer usernames not found");
        }

        trainee.getTrainers().clear();
        trainee.getTrainers().addAll(newTrainers);
        traineeRepository.save(trainee);

        log.info("Updated trainers for trainee '{}': {}", request.username(), usernames);

        return newTrainers.stream().map(Profile.Trainer::from).toList();
    }
}
