package tech.provokedynamic.gymcrm;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.autoconfigure.ssl.SslAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootConfiguration(proxyBeanMethods = false)
@EnableAutoConfiguration(exclude = {
        JmxAutoConfiguration.class,
        SslAutoConfiguration.class,
        TaskExecutionAutoConfiguration.class,
        TaskSchedulingAutoConfiguration.class,
        SpringApplicationAdminJmxAutoConfiguration.class,
})
@EnableAspectJAutoProxy
@ComponentScan(basePackages = {
        "tech.provokedynamic.gymcrm.dao",
        "tech.provokedynamic.gymcrm.config",
        "tech.provokedynamic.gymcrm.service",
        "tech.provokedynamic.gymcrm.component",
        "tech.provokedynamic.gymcrm.facade",
        "tech.provokedynamic.gymcrm.aspect",
})
public class GymCrmApplication {
    static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(GymCrmApplication.class)
                .web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.OFF)
                .logStartupInfo(false)
                .headless(false)
                .build(args)
                .run();
    }
}
