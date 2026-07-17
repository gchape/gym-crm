package tech.provokedynamic.gymcrmauthorizationserver;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootConfiguration(proxyBeanMethods = false)
@ComponentScan(basePackages = {
        "tech.provokedynamic.gymcrmauthorizationserver.config",
        "tech.provokedynamic.gymcrmauthorizationserver.repository",
        "tech.provokedynamic.gymcrmauthorizationserver.security",
})
@EnableAutoConfiguration
@EntityScan(basePackages = {
        "tech.provokedynamic.gymcrmauthorizationserver.entity",
})
public class GymCrmAuthorizationServerApplication {

    static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(GymCrmAuthorizationServerApplication.class)
                .headless(false)
                .logStartupInfo(false)
                .bannerMode(Banner.Mode.OFF)
                .web(WebApplicationType.SERVLET)
                .build(args)
                .run();
    }
}
