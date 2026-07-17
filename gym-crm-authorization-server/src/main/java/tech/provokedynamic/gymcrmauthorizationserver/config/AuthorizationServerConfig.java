package tech.provokedynamic.gymcrmauthorizationserver.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;

@Configuration
public class AuthorizationServerConfig {

    // client_credentials secret used by gym-crm -> gym-crm-workload calls.
    // Same env var already referenced in gym-crm.yaml's oauth2.client config,
    // so both sides must agree on this value.
    @Value("${GYM_CRM_CLIENT_SECRET:dev-secret-change-me}")
    private String gymCrmServiceSecret;

    @Value("${GYM_CRM_FRONTEND_REDIRECT_URIS:http://localhost:3000/callback,http://localhost:4200/callback}")
    private String[] frontendRedirectUris;

    /**
     * Handles everything under /oauth2/** and /.well-known/** (token, jwks,
     * authorize, oidc discovery). Must be ordered before the default chain
     * so its narrower matcher wins.
     */
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

    /**
     * Everything else: the login form itself, actuator health.
     */
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
                .clientSecret(passwordEncoder.encode(gymCrmServiceSecret))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("workload.write")
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(10))
                        .build())
                .build();

        // Public client (SPA): no client secret to protect, so authentication
        // method is NONE and PKCE (requireProofKey) is the actual client-identity
        // proof. If gym-crm-frontend is ever converted to a server-side BFF that
        // performs the code exchange server-side, switch back to
        // CLIENT_SECRET_BASIC with a real secret instead of PKCE-only.
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

        for (String uri : frontendRedirectUris) {
            frontendBuilder.redirectUri(uri.trim());
        }

        return new InMemoryRegisteredClientRepository(gymCrmService, frontendBuilder.build());
    }

    /**
     * Puts the authenticated principal's authorities onto the access token as a
     * "roles" claim, and (implicitly, via RegisteredClient.scope(...)) leaves the
     * standard "scope" claim intact for client_credentials tokens like
     * gym-crm-service. Downstream resource servers (gym-crm, gym-crm-workload)
     * rely on the "scope" claim for hasAuthority("SCOPE_...") checks and can use
     * "roles" for user-identity-based checks going forward.
     * <p>
     * client_credentials tokens have no user Authentication (context.getPrincipal()
     * is a ClientAuthenticationToken, not a user), so the roles branch is skipped
     * for gym-crm-service and only its RegisteredClient scopes apply.
     */
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

    /**
     * Generates an in-memory RSA keypair on startup. Fine for dev — tokens
     * just won't survive a restart (kid changes). For prod, replace with a
     * key loaded from Vault/KMS so restarts don't invalidate every
     * outstanding token, and so multiple auth-server instances behind Eureka
     * share the same signing key.
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();

        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();

        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    /**
     * issuer must exactly match spring.security.oauth2.resourceserver.jwt.issuer-uri
     * in gym-crm.yaml / gym-crm-workload.yaml — every JWT's "iss" claim is
     * checked against it.
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings(
            @Value("${gym-crm.auth-server.issuer:http://localhost:9000}") String issuer) {
        return AuthorizationServerSettings.builder()
                .issuer(issuer)
                .build();
    }
}
