package tech.provokedynamic.gymcrm.config;

import tech.provokedynamic.gymcrm.repository.UserRepository;
import tech.provokedynamic.gymcrm.util.CredentialGenerator;
import tech.provokedynamic.gymcrm.util.DBCredentialGenerator;

@Configuration(proxyBeanMethods = false)
public class CredentialConfig {

    @Bean
    public CredentialGenerator credentialGenerator(UserRepository userRepository) {
        return new DBCredentialGenerator(userRepository);
    }
}
