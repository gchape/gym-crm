package tech.provokedynamic.gymcrm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.provokedynamic.gymcrm.annotations.Validate;
import tech.provokedynamic.gymcrm.component.CredentialGenerator;
import tech.provokedynamic.gymcrm.dao.TrainerDao;
import tech.provokedynamic.gymcrm.dto.TrainerRequest;
import tech.provokedynamic.gymcrm.dto.TrainerResponse;
import tech.provokedynamic.gymcrm.entity.Trainer;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TrainerServiceImpl implements TrainerService {
    private static final Logger log = LoggerFactory.getLogger(TrainerServiceImpl.class);

    private final AtomicLong id = new AtomicLong(1);

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
    @Validate
    public TrainerResponse.Detail create(TrainerRequest.Create request) {
        long nextId = id.getAndIncrement();

        String username = credentialGenerator.generateUsername(
                request.firstName(),
                request.lastName(),
                trainerDao.findAll()
        );
        String password = credentialGenerator.generatePassword();

        Trainer toSave = Trainer.builder()
                .id(nextId)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .username(username)
                .password(password)
                .isActive(true)
                .specialization(request.specialization())
                .build();

        log.debug("Creating trainer with username: {}", username);
        return TrainerResponse.Detail.from(trainerDao.save(nextId, toSave));
    }

    @Override
    @Validate
    public TrainerResponse.Detail update(long id, TrainerRequest.Update request) {
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
        return TrainerResponse.Detail.from(trainerDao.save(id, updated));
    }

    @Override
    public void delete(long id) {
        log.debug("Deleting trainer with id: {}", id);
        trainerDao.delete(id);
    }

    @Override
    public TrainerResponse.Detail findById(long id) {
        log.debug("Finding trainer with id: {}", id);
        return TrainerResponse.Detail.from(
                trainerDao.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Trainer not found with id: " + id))
        );
    }

    @Override
    public List<TrainerResponse.Summary> findAll() {
        log.debug("Finding all trainers");
        return trainerDao.findAll().stream()
                .map(TrainerResponse.Summary::from)
                .toList();
    }
}