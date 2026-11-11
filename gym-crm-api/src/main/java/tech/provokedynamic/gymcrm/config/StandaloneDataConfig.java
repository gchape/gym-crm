package tech.provokedynamic.gymcrm.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import tech.provokedynamic.gymcrm.entity.TrainingType;
import tech.provokedynamic.gymcrm.repository.TrainingTypeRepository;
import java.util.List;

/** Seeds the public training catalog only for the ephemeral H2 demonstration profile. */
@Configuration(proxyBeanMethods = false)
@Profile("standalone")
public class StandaloneDataConfig {
    @Bean
    ApplicationRunner trainingTypes(TrainingTypeRepository repository) {
        return _ -> {
            for (var name : List.of("CARDIO", "STRENGTH", "FLEXIBILITY", "HIIT")) {
                if (repository.findByTrainingTypeName(name).isEmpty()) {
                    repository.save(new TrainingType(name));
                }
            }
        };
    }
}
