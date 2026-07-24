package tech.provokedynamic.gymcrmauthorizationserver.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;

@Configuration
@EnableConfigurationProperties(AuthServerProperties.class)
@RequiredArgsConstructor
public class AuthorizationServerConfig {

    private final AuthServerProperties properties;
    private final ResourceLoader resourceLoader;

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) {
        http
                .oauth2AuthorizationServer(server -> {
                    http.securityMatcher(server.getEndpointsMatcher());
                    server.oidc(Customizer.withDefaults());
                })
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                        .anyRequest().authenticated())
                .formLogin(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(PasswordEncoder passwordEncoder) {
        RegisteredClient gymCrmService = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("gym-crm-service")
                .clientSecret(passwordEncoder.encode(properties.clientSecret()))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("workload.write")
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(10))
                        .build())
                .build();

        RegisteredClient.Builder frontendBuilder = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("gym-crm-frontend")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false)
                        .requireProofKey(true)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(30))
                        .refreshTokenTimeToLive(Duration.ofDays(7))
                        .reuseRefreshTokens(false)
                        .build());

        properties.frontendRedirectUris().forEach(uri -> frontendBuilder.redirectUri(uri.trim()));

        return new InMemoryRegisteredClientRepository(gymCrmService, frontendBuilder.build());
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {
            if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                return;
            }
            var principal = context.getPrincipal();
            if (principal == null) {
                return;
            }
            var roles = principal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
            if (!roles.isEmpty()) {
                context.getClaims().claim("roles", roles);
            }
        };
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() throws Exception {
        var keyStoreProps = properties.keyStore();

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream in = resourceLoader.getResource(keyStoreProps.location()).getInputStream()) {
            keyStore.load(in, keyStoreProps.password().toCharArray());
        }

        RSAPrivateKey privateKey = (RSAPrivateKey) keyStore.getKey(
                keyStoreProps.alias(), keyStoreProps.keyPassword().toCharArray());
        RSAPublicKey publicKey = (RSAPublicKey) keyStore.getCertificate(keyStoreProps.alias()).getPublicKey();

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                // Stable kid derived from the alias rather than a random UUID per
                // boot — lets resource servers cache JWKS across restarts without
                // treating every restart as a full key rotation.
                .keyID(keyStoreProps.alias())
                .build();

        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer(properties.issuer())
                .build();
    }
}
