package tech.provokedynamic.gymcrm.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class Pointcuts {

    @Pointcut("within(tech.provokedynamic.gymcrm.service..*)")
    public void inServiceLayer() {
    }

    @Pointcut("@annotation(tech.provokedynamic.gymcrm.annotation.Validate)")
    public void validateAnnotated() {
    }

    @Pointcut("@annotation(tech.provokedynamic.gymcrm.annotation.Authenticated)")
    public void authenticatedAnnotated() {
    }

    @Pointcut("inServiceLayer() && validateAnnotated()")
    public void validateInService() {
    }

    @Pointcut("inServiceLayer() && authenticatedAnnotated()")
    public void authenticatedInService() {
    }
}
