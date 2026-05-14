package tech.provokedynamic.gymcrm.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.provokedynamic.gymcrm.annotations.Validate;
import tech.provokedynamic.gymcrm.component.CredentialGenerator;
import tech.provokedynamic.gymcrm.dao.TraineeDao;
import tech.provokedynamic.gymcrm.dto.TraineeRequest;
import tech.provokedynamic.gymcrm.dto.TraineeResponse;
import tech.provokedynamic.gymcrm.entity.Trainee;
import tech.provokedynamic.gymcrm.service.TraineeService;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TraineeServiceImpl implements TraineeService {
    private static final Logger log = LoggerFactory.getLogger(TraineeServiceImpl.class);

    private final AtomicLong id = new AtomicLong(1);

    private final TraineeDao traineeDao;
    private CredentialGenerator credentialGenerator;

    public TraineeServiceImpl(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setCredentialGenerator(CredentialGenerator credentialGenerator) {
        this.credentialGenerator = credentialGenerator;
    }

    @Override
    @Validate
    public TraineeResponse.Detail create(TraineeRequest.Create request) {
        long nextId = id.getAndIncrement();

        String username = credentialGenerator.generateUsername(
                request.firstName(),
                request.lastName(),
                traineeDao.findAll()
        );
        String password = credentialGenerator.generatePassword();

        Trainee toSave = Trainee.builder()
                .id(nextId)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .username(username)
                .password(password)
                .isActive(true)
                .dateOfBirth(request.dateOfBirth())
                .address(request.address())
                .build();

        log.debug("Creating trainee with username: {}", username);
        return TraineeResponse.Detail.from(traineeDao.save(nextId, toSave));
    }

    @Override
    @Validate
    public TraineeResponse.Detail update(long id, TraineeRequest.Update request) {
        Trainee existing = traineeDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found with id: " + id));

        Trainee updated = Trainee.builder()
                .id(id)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .username(existing.username())
                .password(existing.password())
                .isActive(request.active())
                .dateOfBirth(request.dateOfBirth())
                .address(request.address())
                .build();

        log.debug("Updating trainee with id: {}", id);
        return TraineeResponse.Detail.from(traineeDao.update(id, updated));
    }

    @Override
    public void delete(long id) {
        log.debug("Deleting trainee with id: {}", id);
        traineeDao.delete(id);
    }

    @Override
    public TraineeResponse.Detail findById(long id) {
        log.debug("Finding trainee with id: {}", id);
        return TraineeResponse.Detail.from(
                traineeDao.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Trainee not found with id: " + id))
        );
    }

    @Override
    public List<TraineeResponse.Summary> findAll() {
        log.debug("Finding all trainees");
        return traineeDao.findAll().stream()
                .map(TraineeResponse.Summary::from)
                .toList();
    }
}
