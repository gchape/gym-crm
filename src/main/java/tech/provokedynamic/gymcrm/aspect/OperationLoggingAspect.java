package tech.provokedynamic.gymcrm.aspect;

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
import tech.provokedynamic.gymcrm.dto.Sensitive;

import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
public class OperationLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLoggingAspect.class);

    @Around("within(tech.provokedynamic.gymcrm.controller..*)")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest httpRequest = currentHttpRequest();
        String endpoint = httpRequest != null
                ? httpRequest.getMethod() + " " + httpRequest.getRequestURI()
                : joinPoint.getSignature().toShortString();

        String requestArgs = describeArgs(joinPoint.getArgs());

        log.info("Incoming request - endpoint=[{}], request=[{}]", endpoint, requestArgs);

        try {
            Object result = joinPoint.proceed();
            int status = (result instanceof ResponseEntity<?> re) ? re.getStatusCode().value() : 200;
            log.info("Request completed - endpoint=[{}], status=[{}]", endpoint, status);
            return result;
        } catch (Exception ex) {
            log.error("Request failed - endpoint=[{}], status=[500], message=[{}]", endpoint, ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Renders each argument for logging, masking anything that implements
     * {@link Sensitive} (passwords, tokens, etc.) instead of relying on its
     * default toString(). Any DTO carrying a secret should implement
     * Sensitive rather than being added here as a special case.
     */
    private String describeArgs(Object[] args) {
        return Arrays.stream(args)
                .map(arg -> arg instanceof Sensitive sensitive ? sensitive.redacted() : String.valueOf(arg))
                .collect(Collectors.joining(", "));
    }

    private HttpServletRequest currentHttpRequest() {
        var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }
}
