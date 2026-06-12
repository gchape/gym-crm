package tech.provokedynamic.gymcrm.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .packagesToScan("tech.provokedynamic.gymcrm.controller")
                .pathsToMatch("/api/**")
                .producesToMatch("application/json")
                .consumesToMatch("application/json")
                .build();
    }
}
