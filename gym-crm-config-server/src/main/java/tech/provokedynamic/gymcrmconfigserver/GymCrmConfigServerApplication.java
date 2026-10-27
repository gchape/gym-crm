package tech.provokedynamic.gymcrmconfigserver;

import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication(proxyBeanMethods = false)
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
