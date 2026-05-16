package tech.provokedynamic.gymcrm.aspect;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.provokedynamic.gymcrm.aspect.pointcuts.AnnotationPointcuts;
import tech.provokedynamic.gymcrm.aspect.pointcuts.ServicePointcuts;
import tech.provokedynamic.gymcrm.dao.TrainerDao;
import tech.provokedynamic.gymcrm.dao.TrainingTypeDao;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.service.TrainerService;
import tech.provokedynamic.gymcrm.service.impl.TrainerServiceImpl;
import tech.provokedynamic.gymcrm.util.CredentialGenerator;
import tech.provokedynamic.gymcrm.validation.impl.RequestValidatorImpl;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        RequestValidationAspectTest.AspectConfig.class,
        AnnotationPointcuts.class,
        ServicePointcuts.class,
        RequestValidatorImpl.class,
        RequestValidationAspect.class,
        TrainerServiceImpl.class
})
class RequestValidationAspectTest {

    @Autowired
    private TrainerService trainerService;

    @MockitoBean
    private TrainerDao trainerDao;

    @MockitoBean
    private TrainingTypeDao trainingTypeDao;

    @MockitoBean
    private CredentialGenerator credentialGenerator;

    @Test
    void validate_doesNotThrow_whenRequestIsValid() {
        var request = new Request.ChangePassword(
                "John.Doe",
                "john.doe@O",
                "john.doe@N"
        );

        assertThatCode(() -> trainerService.changePassword(request))
                .doesNotThrowAnyException();
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
    }
}
