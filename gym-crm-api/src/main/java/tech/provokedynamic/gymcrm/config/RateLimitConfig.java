package tech.provokedynamic.gymcrm.config;

import org.apache.catalina.filters.RateLimitFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registers the per-IP login request budget; app.auth.login-rate-limit configures requests per minute. */
@Configuration
public class RateLimitConfig {

    @Bean
    public FilterRegistrationBean<RateLimitFilter> loginRateLimitFilter(
            @org.springframework.beans.factory.annotation.Value("${app.auth.login-rate-limit:10}") int requestsPerMinute) {
        var filter = new RateLimitFilter();

        filter.setBucketRequests(requestsPerMinute);

        filter.setBucketDuration(60);

        filter.setEnforce(true);

        filter.setStatusCode(429);

        var registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/api/auth/login");
        registration.setName("loginRateLimitFilter");
        registration.setOrder(1);
        return registration;
    }
}
