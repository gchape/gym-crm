package tech.provokedynamic.gymcrm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "gym-crm.auth")
public record AuthProperties(
        String issuer,
        Duration accessTokenTtl,
        KeyStoreProperties keyStore
) {
    public record KeyStoreProperties(
            String location,
            String password,
            String alias,
            String keyPassword
    ) {
    }
}
