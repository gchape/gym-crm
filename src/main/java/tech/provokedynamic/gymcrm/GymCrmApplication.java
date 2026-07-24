package tech.provokedynamic.gymcrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import tech.provokedynamic.gymcrm.exception.GlobalExceptionHandler;
import tech.provokedynamic.gymcrm.repository.UserRepository;
import tech.provokedynamic.gymcrm.util.CredentialGenerator;
import tech.provokedynamic.gymcrm.util.DBCredentialGenerator;

@SpringBootConfiguration(
        proxyBeanMethods = false)
@EntityScan(basePackages = {
        "tech.provokedynamic.gymcrm.entity"
})
@ComponentScan(basePackages = {
        "tech.provokedynamic.gymcrm.aspect",
        "tech.provokedynamic.gymcrm.controller",
        "tech.provokedynamic.gymcrm.config",
        "tech.provokedynamic.gymcrm.repository",
        "tech.provokedynamic.gymcrm.service",
        "tech.provokedynamic.gymcrm.validation",
        "tech.provokedynamic.gymcrm.client",
})
@EnableAutoConfiguration
@Import(GlobalExceptionHandler.class)
@EnableDiscoveryClient
public class GymCrmApplication {

    static void main(String[] args) {
        SpringApplication.run(GymCrmApplication.class, args);
    }

    @Bean
    public CredentialGenerator credentialGenerator(UserRepository userRepository) {
        return new DBCredentialGenerator(userRepository);
    }
}
