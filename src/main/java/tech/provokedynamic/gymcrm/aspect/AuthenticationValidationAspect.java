package tech.provokedynamic.gymcrm.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tech.provokedynamic.gymcrm.dao.AuthDao;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.exception.AuthenticationException;

@Component
@Aspect
public class AuthenticationValidationAspect {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationValidationAspect.class);

    private final AuthDao authDao;

    public AuthenticationValidationAspect(AuthDao authDao) {
        this.authDao = authDao;
    }

    @Before("tech.provokedynamic.gymcrm.aspect.pointcuts.ServicePointcuts.authenticatedInService()")
    public void authenticate(@NonNull JoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Request.Authenticated authenticated) {
                if (!authDao.existsByUsernameAndPassword(authenticated.username(), authenticated.password())) {
                    log.warn("Authentication failed for username '{}'", authenticated.username());
                    throw new AuthenticationException("Invalid username or password");
                }
                return;
            }
        }
    }
}
