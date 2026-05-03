package tech.provokedynamic.gymcrm;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

@SpringBootConfiguration(proxyBeanMethods = false)
@EnableAutoConfiguration(exclude = {
        JmxAutoConfiguration.class,
        TaskExecutionAutoConfiguration.class,
        TaskSchedulingAutoConfiguration.class,
})
@ComponentScan(basePackages = {

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
