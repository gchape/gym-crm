package tech.provokedynamic.gymcrm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.provokedynamic.gymcrm.component.CredentialGenerator;
import tech.provokedynamic.gymcrm.dao.TraineeDao;
import tech.provokedynamic.gymcrm.dto.CreateTraineeRequest;
import tech.provokedynamic.gymcrm.entity.Trainee;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TraineeServiceImpl implements TraineeService {
    private static final Logger log = LoggerFactory.getLogger(TraineeServiceImpl.class);

    private static final AtomicLong ID = new AtomicLong(1);

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
    public Trainee create(CreateTraineeRequest request) {
        long id = ID.getAndIncrement();

        String username = credentialGenerator.generateUsername(
                request.firstName(),
                request.lastName(),
                traineeDao.findAll()
        );
        String password = credentialGenerator.generatePassword();

        Trainee toSave = Trainee.builder()
                .id(id)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .username(username)
                .password(password)
                .isActive(true)
                .dateOfBirth(request.dateOfBirth())
                .address(request.address())
                .build();

        log.debug("Creating trainee with username: {}", username);
        return traineeDao.save(id, toSave);
    }

    @Override
    public Trainee update(long id, CreateTraineeRequest request) {
        Trainee existing = traineeDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found with id: " + id));

        Trainee updated = Trainee.builder()
                .id(id)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .username(existing.getUsername())
                .password(existing.getPassword())
                .isActive(existing.isActive())
                .dateOfBirth(request.dateOfBirth())
                .address(request.address())
                .build();

        log.debug("Updating trainee with id: {}", id);
        return traineeDao.update(id, updated);
    }

    @Override
    public void delete(long id) {
        log.debug("Deleting trainee with id: {}", id);
        traineeDao.delete(id);
    }

    @Override
    public Optional<Trainee> findById(long id) {
        log.debug("Finding trainee with id: {}", id);
        return traineeDao.findById(id);
    }

    @Override
    public List<Trainee> findAll() {
        log.debug("Finding all trainees");
        return traineeDao.findAll();
    }
}
