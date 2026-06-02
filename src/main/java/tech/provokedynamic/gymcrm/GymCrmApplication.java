package tech.provokedynamic.gymcrm;

import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springdoc.webmvc.ui.SwaggerConfig;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration(
        proxyBeanMethods = false)
@PropertySource(
        "classpath:application.properties")
@EnableJpaRepositories(basePackages = {
        "tech.provokedynamic.gymcrm.repository"
})
@Import(value = {
        SpringDocConfiguration.class,
        SpringDocWebMvcConfiguration.class,
        SwaggerConfig.class
})
@EnableAspectJAutoProxy
@EnableWebMvc
public class GymCrmApplication {

    private static final String[] basePackages = {
            "tech.provokedynamic.gymcrm.aspect",
            "tech.provokedynamic.gymcrm.validation",
            "tech.provokedynamic.gymcrm.service",
            "tech.provokedynamic.gymcrm.controller",
            "tech.provokedynamic.gymcrm.config",
    };

    static void main() {
        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.register(GymCrmApplication.class);
            ctx.getEnvironment().setActiveProfiles("dev");
            ctx.scan(basePackages);
            ctx.refresh();
        }
    }
}
