package tech.provokedynamic.gymcrm.aspect;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import tech.provokedynamic.gymcrm.aspect.pointcuts.AnnotationPointcuts;
import tech.provokedynamic.gymcrm.aspect.pointcuts.ServicePointcuts;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.repository.TrainerRepository;
import tech.provokedynamic.gymcrm.repository.TrainingRepository;
import tech.provokedynamic.gymcrm.repository.TrainingTypeRepository;
import tech.provokedynamic.gymcrm.service.TrainerService;
import tech.provokedynamic.gymcrm.service.impl.TrainerServiceImpl;
import tech.provokedynamic.gymcrm.util.DBCredentialGenerator;
import tech.provokedynamic.gymcrm.validation.RequestValidator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        RequestValidationAspectTest.AspectConfig.class,
        ServicePointcuts.class,
        AnnotationPointcuts.class,
        RequestValidator.class,
        RequestValidationAspect.class,
        TrainerServiceImpl.class
})
class RequestValidationAspectTest {

    @Autowired
    private TrainerService trainerService;

    @MockitoBean
    private TrainerRepository trainerRepository;
    @MockitoBean
    private TrainingRepository trainingRepository;
    @MockitoBean
    private TrainingTypeRepository trainingTypeRepository;
    @MockitoBean
    private DBCredentialGenerator credentialGenerator;

    @Test
    void validate_doesNotThrow_whenRequestIsValid() {
        var request = new Request.ChangePassword(
                "John.Doe",
                "john.doe@O",
                "john.doe@N"
        );

        assertThatThrownBy(() -> trainerService.changePassword(request))
                .isNotInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void validate_throws_whenRequestHasBlankUsername() {
        var request = new Request.ChangePassword(
                "",
                "john.doe@O",
                "john.doe@N"
        );

        assertThatThrownBy(() -> trainerService.changePassword(request))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void validate_throws_whenRequestHasInvalidNewPassword() {
        var request = new Request.ChangePassword(
                "John.Doe",
                "john.doe@O",
                "short"
        );

        assertThatThrownBy(() -> trainerService.changePassword(request))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Configuration
    @EnableAspectJAutoProxy
    static class AspectConfig {
        @Bean
        public jakarta.validation.Validator validator() {
            return new LocalValidatorFactoryBean();
        }

        @Bean
        public BCryptPasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }
}
