package tech.provokedynamic.gymcrmeurekaserver;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootConfiguration(
        proxyBeanMethods = false)
@EnableAutoConfiguration
@ComponentScan(basePackages = {
        "tech.provokedynamic.gymcrmeurekaserver",
        "tech.provokedynamic.gymcrmeurekaserver.config",
})
@EnableEurekaServer
public class GymCrmEurekaServerApplication {

    static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(GymCrmEurekaServerApplication.class)
                .logStartupInfo(false)
                .bannerMode(Banner.Mode.OFF)
                .web(WebApplicationType.SERVLET)
                .allowCircularReferences(false)
                .headless(true)
                .build(args)
                .run();
    }
}
