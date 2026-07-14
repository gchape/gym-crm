package tech.provokedynamic.gymcrmworkload.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Aspect
@Component
public class OperationLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLoggingAspect.class);

    @Around("within(tech.provokedynamic.gymcrmworkload.controller..*)")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest httpRequest = currentHttpRequest();
        String endpoint = httpRequest != null
                ? httpRequest.getMethod() + " " + httpRequest.getRequestURI()
                : joinPoint.getSignature().toShortString();

        String requestArgs = Arrays.toString(joinPoint.getArgs());
        log.info("Incoming request - endpoint=[{}], request=[{}]", endpoint, requestArgs);

        try {
            Object result = joinPoint.proceed();

            int status = (result instanceof ResponseEntity<?> responseEntity)
                    ? responseEntity.getStatusCode().value()
                    : 200;

            log.info("Request completed - endpoint=[{}], status=[{}]", endpoint, status);
            return result;

        } catch (Exception ex) {
            log.error("Request failed - endpoint=[{}], status=[500], message=[{}]",
                    endpoint, ex.getMessage(), ex);
            throw ex;
        }
    }

    private HttpServletRequest currentHttpRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }
}
