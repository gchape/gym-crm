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

        for (JsonNode node : root.get("trainees")) {
            long id = node.get("id").asLong();
            Trainee trainee = jsonMapper.treeToValue(node, Trainee.class);
            storage.put("trainee", id, trainee);
            log.debug("Loaded trainee: {}", trainee.getUsername());
        }

        for (JsonNode node : root.get("trainers")) {
            long id = node.get("id").asLong();
            Trainer trainer = jsonMapper.treeToValue(node, Trainer.class);
            storage.put("trainer", id, trainer);
            log.debug("Loaded trainer: {}", trainer.getUsername());
        }

        for (JsonNode node : root.get("trainings")) {
            long id = node.get("id").asLong();
            Training training = jsonMapper.treeToValue(node, Training.class);
            storage.put("training", id, training);
            log.debug("Loaded training: {}", training.trainingName());
        }

        log.info("Storage initialized from {}", dataPath);
        System.out.println(storage);
    }
}
