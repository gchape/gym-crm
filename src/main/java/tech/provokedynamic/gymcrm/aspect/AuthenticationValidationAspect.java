package tech.provokedynamic.gymcrm.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.exception.AuthenticationException;
import tech.provokedynamic.gymcrm.repository.UserRepository;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthenticationValidationAspect implements Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationValidationAspect.class);

    private final UserRepository userRepository;

    @Before("tech.provokedynamic.gymcrm.aspect.pointcuts.ServicePointcuts.authenticatedInService()")
    public void authenticate(@NonNull JoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Request.Authenticated authenticated) {
                if (!userRepository.existsByUsernameAndPassword(authenticated.username(), authenticated.password())) {
                    log.warn("Authentication failed for username '{}' attempting '{}'",
                            authenticated.username(),
                            joinPoint.getSignature().getName());
                    throw new AuthenticationException("Invalid username or password");
                }
                return;
            }
        }
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
