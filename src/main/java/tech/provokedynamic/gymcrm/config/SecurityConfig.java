package tech.provokedynamic.gymcrm.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tech.provokedynamic.gymcrm.security.GymCRMUserDetailsService;
import tech.provokedynamic.gymcrm.security.JwtFilter;

import java.security.SecureRandom;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    // BCrypt work factor. 4 (the previous value) is the *minimum* allowed by
    // the library — fast, but far too weak for password storage. 10 is
    // Spring Security's own default; bump further only if your login-latency
    // budget allows it.
    private static final int BCRYPT_STRENGTH = 10;

    private final GymCRMUserDetailsService userDetailsService;
    private final JwtFilter jwtFilter;

    // Comma-separated list of origins allowed to call this API with credentials.
    // Supplied via gym-crm-config-server (app.cors.allowed-origins), defaulting
    // to common local dev origins so the service still boots standalone.
    @Value("#{'${app.cors.allowed-origins:http://localhost:3000,http://localhost:4200}'.split(',')}")
    private List<String> allowedOrigins;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(
                BCryptPasswordEncoder.BCryptVersion.$2Y,
                BCRYPT_STRENGTH,
                new SecureRandom()
        );
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        var provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) ->
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing or invalid authentication token");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
                .cors(
                        configurer -> configurer
                                .configurationSource(corsConfigurationSource())
                )
                .csrf(
                        AbstractHttpConfigurer::disable
                )
                .sessionManagement(
                        session -> session
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .exceptionHandling(
                        exceptions -> exceptions
                                .authenticationEntryPoint(authenticationEntryPoint())
                )
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers(HttpMethod.POST, "/api/trainees").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/trainers").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/login").permitAll()
                                .requestMatchers("/api/training-types").permitAll()
                                .requestMatchers(
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/v3/api-docs/**"
                                ).permitAll()
                                .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private UrlBasedCorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Transaction-Id"));
        config.setExposedHeaders(List.of("X-Transaction-Id"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
