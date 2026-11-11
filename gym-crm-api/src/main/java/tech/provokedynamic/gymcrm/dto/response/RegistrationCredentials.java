package tech.provokedynamic.gymcrm.dto.response;



/** One-time registration credentials. Persist only the password hash, never this response. */
public record RegistrationCredentials(
        String username,
        String password
) {
}
