package tech.provokedynamic.gymcrm.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * gym-crm now issues and validates its own JWTs directly instead of
 * delegating to a separate OAuth2 authorization server. The keypair that
 * used to live in gym-crm-authorization-server's keystore moves here
 * unchanged — only the issuing mechanism changes (a plain login endpoint
 * instead of an OAuth2 grant).
 */
@Configuration
@EnableConfigurationProperties(AuthProperties.class)
@RequiredArgsConstructor
public class JwtConfig {

    private final AuthProperties properties;
    private final ResourceLoader resourceLoader;

    @Bean
    public KeyPair rsaKeyPair() throws Exception {
        var keyStoreProps = properties.keyStore();

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream in = resourceLoader.getResource(keyStoreProps.location()).getInputStream()) {
            keyStore.load(in, keyStoreProps.password().toCharArray());
        }

        RSAPrivateKey privateKey = (RSAPrivateKey) keyStore.getKey(
                keyStoreProps.alias(), keyStoreProps.keyPassword().toCharArray());
        RSAPublicKey publicKey = (RSAPublicKey) keyStore.getCertificate(keyStoreProps.alias()).getPublicKey();

        return new KeyPair(publicKey, privateKey);
    }

    @Bean
    public JwtEncoder jwtEncoder(KeyPair keyPair) {
        RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .keyID(properties.keyStore().alias())
                .build();

        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
    }

    @Bean
    public JwtDecoder jwtDecoder(KeyPair keyPair) {
        return NimbusJwtDecoder.withPublicKey((RSAPublicKey) keyPair.getPublic()).build();
    }
}
