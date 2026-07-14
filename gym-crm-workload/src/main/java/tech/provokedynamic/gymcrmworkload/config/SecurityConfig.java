package tech.provokedynamic.gymcrmworkload.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tech.provokedynamic.gymcrmworkload.filter.BearerAuthFilter;
import tech.provokedynamic.gymcrmworkload.security.JwtValidationService;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtValidationService jwtValidationService;

    // Narrowed from "/actuator/**" — only expose liveness/info, not the full
    // actuator surface (env, beans, heapdump, etc.) without auth.
    private static final String[] PUBLIC_PATHS = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/actuator/health",
            "/actuator/health/**",
            "/actuator/info",
            "/favicon.ico"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .formLogin(AbstractHttpConfigurer::disable)

                .httpBasic(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .anyRequest().authenticated())

                .exceptionHandling(ex -> ex.authenticationEntryPoint((_, response, _) ->
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid bearer token")))

                .addFilterBefore(new BearerAuthFilter(jwtValidationService), UsernamePasswordAuthenticationFilter.class)

                .build();
    }
}
