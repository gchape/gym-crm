package tech.provokedynamic.gymcrm.aspect;

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
import tech.provokedynamic.gymcrm.dao.TraineeDao;
import tech.provokedynamic.gymcrm.dao.TrainerDao;
import tech.provokedynamic.gymcrm.dao.UserDao;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.exception.AuthenticationException;
import tech.provokedynamic.gymcrm.service.TraineeService;
import tech.provokedynamic.gymcrm.service.impl.TraineeServiceImpl;
import tech.provokedynamic.gymcrm.util.CredentialGenerator;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        AuthenticationValidationAspectTest.AspectConfig.class,
        AuthenticationValidationAspect.class,
        AnnotationPointcuts.class,
        ServicePointcuts.class,
        TraineeServiceImpl.class
})
class AuthenticationValidationAspectTest {

    @Autowired
    private TraineeService traineeService;

    @MockitoBean
    private TraineeDao traineeDao;

    @MockitoBean
    private TrainerDao trainerDao;

    @MockitoBean
    private CredentialGenerator credentialGenerator;

    @MockitoBean(name = "userDaoImpl")
    private UserDao userDao;

    @Test
    void authenticate_throws_whenCredentialsAreInvalid() {
        when(userDao.existsByUsernameAndPassword("John.Doe", "password"))
                .thenReturn(false);

        var request = new Request.ToggleActive("John.Doe", "password");

        assertThatThrownBy(() -> traineeService.activate(request))
                .isInstanceOf(AuthenticationException.class);

        verify(userDao).existsByUsernameAndPassword(any(), any());
    }

    @Test
    void authenticate_doesNotThrow_whenCredentialsAreValid() {
        when(userDao.existsByUsernameAndPassword("John.Doe", "password"))
                .thenReturn(true);

        when(traineeDao.activateByUsername("John.Doe"))
                .thenReturn(1);

        var request = new Request.ToggleActive("John.Doe", "password");

        assertThatCode(() -> traineeService.activate(request))
                .doesNotThrowAnyException();
    }

    @Configuration
    @EnableAspectJAutoProxy
    static class AspectConfig {
    }
}
