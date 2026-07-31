package tech.provokedynamic.gymcrm;

import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import tech.provokedynamic.gymcrm.exception.GlobalExceptionHandler;

@SpringBootApplication(proxyBeanMethods = false)
@EntityScan(basePackages = {
        "tech.provokedynamic.gymcrm.entity"
})
@Import(GlobalExceptionHandler.class)
@EnableDiscoveryClient
public class GymCrmApplication {

    static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(GymCrmApplication.class)
                .headless(true)
                .logStartupInfo(false)
                .bannerMode(Banner.Mode.OFF)
                .web(WebApplicationType.SERVLET)
                .build(args)
                .run();
    }
}
