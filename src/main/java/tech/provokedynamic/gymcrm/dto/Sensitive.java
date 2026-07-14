package tech.provokedynamic.gymcrm.dto;

/**
 * Marker for request/response DTOs that carry secrets (passwords, tokens, etc.).
 * Implementations must return a {@link #redacted()} representation with those
 * fields masked. {@link tech.provokedynamic.gymcrm.aspect.OperationLoggingAspect}
 * checks for this interface before logging method arguments, so any class
 * carrying a secret should implement it rather than relying on default
 * toString().
 */
public interface Sensitive {

    String redacted();
}
