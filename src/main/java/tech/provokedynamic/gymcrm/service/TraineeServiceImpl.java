package tech.provokedynamic.gymcrm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.provokedynamic.gymcrm.annotations.Validate;
import tech.provokedynamic.gymcrm.component.CredentialGenerator;
import tech.provokedynamic.gymcrm.dao.TraineeDao;
import tech.provokedynamic.gymcrm.dto.TraineeRequest;
import tech.provokedynamic.gymcrm.entity.Trainee;

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
    public Trainee create(TraineeRequest.Create request) {
        long nextId = id.getAndIncrement();

        String username = credentialGenerator.generateUsername(
                request.getFirstName(),
                request.getLastName(),
                traineeDao.findAll()
        );
        String password = credentialGenerator.generatePassword();

        Trainee toSave = Trainee.builder()
                .id(nextId)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(username)
                .password(password)
                .isActive(true)
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .build();

        log.debug("Creating trainee with username: {}", username);
        return traineeDao.save(nextId, toSave);
    }

    @Override
    @Validate
    public Trainee update(long id, TraineeRequest.Update request) {
        Trainee existing = traineeDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found with id: " + id));

        Trainee updated = Trainee.builder()
                .id(id)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(existing.getUsername())
                .password(existing.getPassword())
                .isActive(request.isActive())
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
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
    public Trainee findById(long id) {
        log.debug("Finding trainee with id: {}", id);
        return traineeDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found with id: " + id));
    }

    @Override
    public List<Trainee> findAll() {
        log.debug("Finding all trainees");
        return traineeDao.findAll();
    }
}