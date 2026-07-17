package tech.provokedynamic.gymcrmworkload;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootConfiguration(
        proxyBeanMethods = false)
@EnableAutoConfiguration
@ComponentScan(basePackages = {
        "tech.provokedynamic.gymcrmworkload.config",
        "tech.provokedynamic.gymcrmworkload.aspect",
        "tech.provokedynamic.gymcrmworkload.filter",
        "tech.provokedynamic.gymcrmworkload.service",
        "tech.provokedynamic.gymcrmworkload.controller",
        "tech.provokedynamic.gymcrmworkload.mapper",
})
@EnableDiscoveryClient
public class GymCrmWorkloadApplication {

    static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(GymCrmWorkloadApplication.class)
                .logStartupInfo(false)
                .bannerMode(Banner.Mode.OFF)
                .web(WebApplicationType.SERVLET)
                .allowCircularReferences(false)
                .headless(true)
                .build(args)
                .run();
    }
}
