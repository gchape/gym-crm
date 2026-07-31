package tech.provokedynamic.gymcrm.service;

public interface LoginService {

    /**
     * Verifies username/password against gym-crm's own user table and
     * returns a signed access token on success.
     */
    String login(String username, String rawPassword);
}
