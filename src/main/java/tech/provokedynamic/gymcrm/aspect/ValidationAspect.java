package tech.provokedynamic.gymcrm.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.validation.BeanValidator;

@Aspect
@Component
public class ValidationAspect {
    private static final Logger log = LoggerFactory.getLogger(ValidationAspect.class);

    @Pointcut("@annotation(tech.provokedynamic.gymcrm.annotations.Validate)")
    public void validateTarget() {
    }

    @Before("validateTarget() && within(tech.provokedynamic.gymcrm.service.*)")
    public void validate(JoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Request request) {
                log.debug("Validating {} in {}",
                        request.getClass().getSimpleName(),
                        joinPoint.getSignature().getName());
                BeanValidator.INSTANCE.validate(request);
            }
        }
    }
}
