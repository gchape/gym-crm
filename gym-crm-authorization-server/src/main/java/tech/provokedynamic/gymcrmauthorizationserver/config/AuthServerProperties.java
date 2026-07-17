package tech.provokedynamic.gymcrmauthorizationserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "gym-crm.auth-server")
public record AuthServerProperties(String issuer, String clientSecret, List<String> frontendRedirectUris) {
}
