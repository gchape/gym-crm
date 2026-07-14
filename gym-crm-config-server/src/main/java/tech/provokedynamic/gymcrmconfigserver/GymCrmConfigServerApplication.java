package tech.provokedynamic.gymcrmconfigserver;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootConfiguration(
        proxyBeanMethods = false)
@EnableAutoConfiguration
@ComponentScan(basePackages = {
        "tech.provokedynamic.gymcrmconfigserver.config",
})
@EnableConfigServer
public class GymCrmConfigServerApplication {

    static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(GymCrmConfigServerApplication.class)
                .logStartupInfo(false)
                .bannerMode(Banner.Mode.OFF)
                .web(WebApplicationType.SERVLET)
                .allowCircularReferences(false)
                .headless(true)
                .build(args)
                .run();
    }
}
