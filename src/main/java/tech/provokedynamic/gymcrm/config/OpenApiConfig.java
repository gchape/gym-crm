package tech.provokedynamic.gymcrm.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI(SpecVersion.V31)
                .info(new Info()
                        .title("Gym CRM API")
                        .description("Gym management system")
                        .version("1.0")
                        .contact(new Contact()
                                .email("chapidze.giorgi@proton.me")));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .packagesToScan("tech.provokedynamic.gymcrm.controller")
                .pathsToMatch("/api/**")
                .build();
    }
}
