package tech.provokedynamic.gymcrm;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.context.LifecycleAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import tools.jackson.databind.json.JsonMapper;

@SpringBootConfiguration(
        proxyBeanMethods = false)
@ImportAutoConfiguration(classes = {
        AopAutoConfiguration.class,
        LifecycleAutoConfiguration.class,
        PropertyPlaceholderAutoConfiguration.class,
//      ProjectInfoAutoConfiguration.class,
//      ConfigurationPropertiesAutoConfiguration.class,
})
@AutoConfigurationPackage(
        basePackageClasses = GymCrmApplication.class)
@ComponentScan(basePackages = {
        "tech.provokedynamic.gymcrm.dao",
        "tech.provokedynamic.gymcrm.service",
        "tech.provokedynamic.gymcrm.component",
        "tech.provokedynamic.gymcrm.facade",
        "tech.provokedynamic.gymcrm.aspect",
})
@EnableAspectJAutoProxy
public class GymCrmApplication {
    static void main(String[] args) {
        var ctx = new SpringApplicationBuilder()
                .sources(GymCrmApplication.class)
                .web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.OFF)
                .logStartupInfo(false)
                .headless(false)
                .build(args)
                .run();

        System.out.println(ctx.getBeanDefinitionCount());
    }

    @Bean
    public JsonMapper jsonMapper() {
        return JsonMapper.builder()
                .build();
    }
}
