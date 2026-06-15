package tech.provokedynamic.gymcrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import tech.provokedynamic.gymcrm.repository.UserRepository;
import tech.provokedynamic.gymcrm.util.CredentialGenerator;
import tech.provokedynamic.gymcrm.util.DBCredentialGenerator;

@SpringBootApplication(scanBasePackages = {
        "tech.provokedynamic.gymcrm.aspect",
        "tech.provokedynamic.gymcrm.validation",
        "tech.provokedynamic.gymcrm.service",
        "tech.provokedynamic.gymcrm.controller",
        "tech.provokedynamic.gymcrm.config",
        "tech.provokedynamic.gymcrm.security",
        "tech.provokedynamic.gymcrm.repository",
}, proxyBeanMethods = false)
@EntityScan("tech.provokedynamic.gymcrm.entity")
public class GymCrmApplication {

    static void main(String[] args) {
        SpringApplication.run(GymCrmApplication.class, args);
    }

    @Bean
    public CredentialGenerator credentialGenerator(UserRepository userRepository) {
        return new DBCredentialGenerator(userRepository);
    }
}
