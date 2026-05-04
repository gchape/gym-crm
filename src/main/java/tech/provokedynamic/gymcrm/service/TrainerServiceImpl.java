package tech.provokedynamic.gymcrm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.provokedynamic.gymcrm.component.CredentialGenerator;
import tech.provokedynamic.gymcrm.dao.TrainerDao;
import tech.provokedynamic.gymcrm.dto.CreateTrainerRequest;
import tech.provokedynamic.gymcrm.entity.Trainer;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TrainerServiceImpl implements TrainerService {
    private static final Logger log = LoggerFactory.getLogger(TrainerServiceImpl.class);

    private static final AtomicLong ID = new AtomicLong(1);

    private final TrainerDao trainerDao;
    private CredentialGenerator credentialGenerator;

    public TrainerServiceImpl(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setCredentialGenerator(CredentialGenerator credentialGenerator) {
        this.credentialGenerator = credentialGenerator;
    }

    @Override
    public Trainer create(CreateTrainerRequest request) {
        long id = ID.getAndIncrement();

        String username = credentialGenerator.generateUsername(
                request.firstName(),
                request.lastName(),
                trainerDao.findAll()
        );
        String password = credentialGenerator.generatePassword();

        Trainer toSave = Trainer.builder()
                .id(id)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .username(username)
                .password(password)
                .isActive(true)
                .specialization(request.specialization())
                .build();

        log.debug("Creating trainer with username: {}", username);
        return trainerDao.save(id, toSave);
    }

    @Override
    public Trainer update(long id, CreateTrainerRequest request) {
        Trainer existing = trainerDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found with id: " + id));

        Trainer updated = Trainer.builder()
                .id(id)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .username(existing.getUsername())
                .password(existing.getPassword())
                .isActive(existing.isActive())
                .specialization(request.specialization())
                .build();

        log.debug("Updating trainer with id: {}", id);
        return trainerDao.update(id, updated);
    }

    @Override
    public Optional<Trainer> findById(long id) {
        log.debug("Finding trainer with id: {}", id);
        return trainerDao.findById(id);
    }

    @Override
    public List<Trainer> findAll() {
        log.debug("Finding all trainers");
        return trainerDao.findAll();
    }
}
