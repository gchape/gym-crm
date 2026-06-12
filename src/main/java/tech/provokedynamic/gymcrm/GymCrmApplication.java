package tech.provokedynamic.gymcrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication(scanBasePackages = {
        "tech.provokedynamic.gymcrm.aspect",
        "tech.provokedynamic.gymcrm.validation",
        "tech.provokedynamic.gymcrm.service",
        "tech.provokedynamic.gymcrm.controller",
        "tech.provokedynamic.gymcrm.config",
}, proxyBeanMethods = false)
@EntityScan("tech.provokedynamic.gymcrm.entity")
public class GymCrmApplication {

    static void main(String[] args) {
        SpringApplication.run(GymCrmApplication.class, args);
    }
}
