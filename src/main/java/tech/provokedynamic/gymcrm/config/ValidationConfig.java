package tech.provokedynamic.gymcrm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.validation.RequestValidator;
import tech.provokedynamic.gymcrm.validation.Validator;

@Configuration(proxyBeanMethods = false)
public class ValidationConfig {

    @Bean
    public LocalValidatorFactoryBean validatorFactory() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    public Validator<Request> requestValidator(jakarta.validation.Validator validator) {
        return new RequestValidator(validator);
    }
}
