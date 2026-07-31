package tech.provokedynamic.gymcrmworkload;

import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(proxyBeanMethods = false)
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
