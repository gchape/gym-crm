package tech.provokedynamic.gymcrm.aspect.pointcuts;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public final class AnnotationPointcuts {

    @Pointcut("@annotation(tech.provokedynamic.gymcrm.annotation.Validate)")
    public void validateAnnotated() {
    }

    @Pointcut("@annotation(tech.provokedynamic.gymcrm.annotation.Authenticated)")
    public void authenticatedAnnotated() {
    }
}
