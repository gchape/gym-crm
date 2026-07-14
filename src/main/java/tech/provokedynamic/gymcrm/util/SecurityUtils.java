package tech.provokedynamic.gymcrm.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import tech.provokedynamic.gymcrm.exception.ForbiddenException;

/**
 * Small helper around the current {@code SecurityContext}, used by service
 * methods to enforce that a request only mutates the caller's own resource.
 *
 * NOTE: this project has no admin/role concept today — every authenticated
 * user (trainee or trainer) can only act on their own account. If an admin
 * role is introduced later, {@link #requireSelf} is the place to add an
 * "or has ADMIN authority" escape hatch.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityUtils {

    public static String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    /**
     * Throws {@link ForbiddenException} unless the currently authenticated
     * user matches {@code targetUsername}.
     */
    public static void requireSelf(String targetUsername) {
        String current = currentUsername();
        if (current == null || !current.equals(targetUsername)) {
            throw new ForbiddenException("You may only perform this action on your own account");
        }
    }
}
