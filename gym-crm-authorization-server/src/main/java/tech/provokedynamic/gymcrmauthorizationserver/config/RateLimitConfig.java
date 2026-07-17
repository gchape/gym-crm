package tech.provokedynamic.gymcrmauthorizationserver.config;

import org.apache.catalina.filters.RateLimitFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

    @Bean
    public FilterRegistrationBean<RateLimitFilter> tokenEndpointRateLimitFilter() {
        var filter = new RateLimitFilter();

        // Number of requests allowed per bucket duration, per IP
        filter.setBucketRequests(10);
        // Duration of each bucket, in seconds
        filter.setBucketDuration(60);
        // Enforce limit — set false to only log without blocking
        filter.setEnforce(true);
        // HTTP status returned when the limit is exceeded
        filter.setStatusCode(429);

        var registration = new FilterRegistrationBean<>(filter);
        // /oauth2/token is where credential/refresh-token brute-forcing actually
        // happens; /login covers the browser-facing formLogin page for the
        // authorization_code flow.
        registration.addUrlPatterns("/oauth2/token", "/login");
        registration.setName("tokenEndpointRateLimitFilter");
        registration.setOrder(1);
        return registration;
    }
}
