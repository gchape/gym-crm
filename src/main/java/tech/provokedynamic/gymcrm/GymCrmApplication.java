package tech.provokedynamic.gymcrm;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;

@Configuration(proxyBeanMethods = false)
@PropertySource("classpath:application.properties")
@EnableAspectJAutoProxy
public class GymCrmApplication {

    static String[] basePackages = {
            "tech.provokedynamic.gymcrm.dao",
            "tech.provokedynamic.gymcrm.service",
            "tech.provokedynamic.gymcrm.component",
            "tech.provokedynamic.gymcrm.facade",
            "tech.provokedynamic.gymcrm.aspect",
            "tech.provokedynamic.gymcrm.config"
    };

    static void main() {
        var ctx = new AnnotationConfigApplicationContext();
        ctx.register(GymCrmApplication.class);
        ctx.scan(basePackages);
        ctx.refresh();
    }
}
