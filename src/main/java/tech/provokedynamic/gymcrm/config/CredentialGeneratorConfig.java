package tech.provokedynamic.gymcrm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.provokedynamic.gymcrm.repository.UserRepository;
import tech.provokedynamic.gymcrm.util.CredentialGenerator;
import tech.provokedynamic.gymcrm.util.RepositoryCredentialGenerator;

@Configuration(proxyBeanMethods = false)
public class CredentialGeneratorConfig {

    @Bean
    public CredentialGenerator credentialGenerator(UserRepository userRepository) {
        return new RepositoryCredentialGenerator(userRepository);
    }
}
