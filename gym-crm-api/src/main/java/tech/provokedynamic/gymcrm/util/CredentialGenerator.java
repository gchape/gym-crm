package tech.provokedynamic.gymcrm.util;

public interface CredentialGenerator {

    String generatePassword();

    String generateUsername(String firstName, String lastName);
}
