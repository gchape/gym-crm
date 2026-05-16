package tech.provokedynamic.gymcrm.aspect.pointcuts;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public final class ServicePointcuts {

    @Pointcut("within(tech.provokedynamic.gymcrm.service..*)")
    private void inServiceLayer() {
    }

    @Pointcut("tech.provokedynamic.gymcrm.aspect.pointcuts.AnnotationPointcuts.validateAnnotated() && inServiceLayer()")
    public void validateInService() {
    }

    @Pointcut("tech.provokedynamic.gymcrm.aspect.pointcuts.AnnotationPointcuts.authenticatedAnnotated() && inServiceLayer()")
    public void authenticatedInService() {
    }
}
