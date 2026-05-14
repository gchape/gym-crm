package tech.provokedynamic.gymcrm.component;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import tech.provokedynamic.gymcrm.entity.Entity;
import tech.provokedynamic.gymcrm.entity.Trainee;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.entity.Training;
import tech.provokedynamic.gymcrm.storage.Storage;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

@Component
public class StorageInitializer {
    private static final Logger log = LoggerFactory.getLogger(StorageInitializer.class);

    private final Storage<Entity> storage;
    private final JsonMapper jsonMapper;
    private final ResourceLoader resourceLoader;

    @Value("${storage.data.path}")
    private String dataPath;

    public StorageInitializer(Storage<Entity> storage,
                              JsonMapper jsonMapper,
                              ResourceLoader resourceLoader) {
        this.storage = storage;
        this.jsonMapper = jsonMapper;
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() throws IOException {
        var resource = resourceLoader.getResource(dataPath);
        var root = jsonMapper.readTree(resource.getInputStream());

        loadTrainees(root);
        loadTrainers(root);
        loadTrainings(root);

        log.info("Storage initialized from {}", dataPath);
    }

    private void loadTrainees(JsonNode root) {
        JsonNode nodes = root.get("trainees");
        if (nodes == null || !nodes.isArray()) {
            log.warn("No trainees found in {}", dataPath);
            return;
        }
        for (JsonNode node : nodes) {
            long id = node.get("id").asLong();
            Trainee trainee = jsonMapper.treeToValue(node, Trainee.class);
            storage.put(Storage.Namespace.TRAINEE, id, trainee);
            log.debug("Loaded trainee: {}", trainee.username());
        }
    }

    private void loadTrainers(JsonNode root) {
        JsonNode nodes = root.get("trainers");
        if (nodes == null || !nodes.isArray()) {
            log.warn("No trainers found in {}", dataPath);
            return;
        }
        for (JsonNode node : nodes) {
            long id = node.get("id").asLong();
            Trainer trainer = jsonMapper.treeToValue(node, Trainer.class);
            storage.put(Storage.Namespace.TRAINER, id, trainer);
            log.debug("Loaded trainer: {}", trainer.username());
        }
    }

    private void loadTrainings(JsonNode root) {
        JsonNode nodes = root.get("trainings");
        if (nodes == null || !nodes.isArray()) {
            log.warn("No trainings found in {}", dataPath);
            return;
        }
        for (JsonNode node : nodes) {
            long id = node.get("id").asLong();
            Training training = jsonMapper.treeToValue(node, Training.class);
            storage.put(Storage.Namespace.TRAINING, id, training);
            log.debug("Loaded training: {}", training.trainingName());
        }
    }
}
