package tech.provokedynamic.gymcrm.config;

import org.apache.catalina.filters.RateLimitFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

    @Bean
    public FilterRegistrationBean<RateLimitFilter> loginRateLimitFilter() {
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
        registration.addUrlPatterns("/api/auth/login");
        registration.setName("loginRateLimitFilter");
        registration.setOrder(1);
        return registration;
    }
}
