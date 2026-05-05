package tech.provokedynamic.gymcrm.component;

import org.junit.jupiter.api.Test;
import tech.provokedynamic.gymcrm.entity.Trainee;
import tech.provokedynamic.gymcrm.entity.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CredentialGeneratorTest {

    CredentialGenerator credentialGenerator = new CredentialGenerator();

    @Test
    void shouldGenerateUsernameWhenNoUsersExist() {
        List<User> users = List.of();

        String username = credentialGenerator.generateUsername("John", "Doe", users);

        assertThat(username).isEqualTo("John.Doe");
    }

    @Test
    void shouldAppendSuffixWhenUsernameAlreadyExists() {
        User existing = Trainee.builder()
                .firstName("John")
                .lastName("Doe")
                .build();

        List<User> users = List.of(existing);

        String username = credentialGenerator.generateUsername("John", "Doe", users);

        assertThat(username).isEqualTo("John.Doe1");
    }

    @Test
    void shouldIncrementSuffixWhenMultipleUsersExist() {
        List<User> users = List.of(
                Trainee.builder().firstName("John").lastName("Doe").build(),
                Trainee.builder().firstName("John").lastName("Doe").build()
        );

        String username = credentialGenerator.generateUsername("John", "Doe", users);

        assertThat(username).isEqualTo("John.Doe2");
    }

    @Test
    void shouldGeneratePasswordWithLengthTenWhenCalled() {
        String password = credentialGenerator.generatePassword();

        assertThat(password).hasSize(10);
    }

    @Test
    void shouldGenerateDifferentPasswordsWhenCalledMultipleTimes() {
        Set<String> passwords = new HashSet<>();

        for (int i = 0; i < 20; i++) {
            passwords.add(credentialGenerator.generatePassword());
        }

        assertThat(passwords.size()).isGreaterThan(1);
    }

    @Test
    void shouldGenerateNonTrivialPasswordWhenGenerated() {
        String password = credentialGenerator.generatePassword();

        long distinctChars = password.chars().distinct().count();

        assertThat(distinctChars).isGreaterThan(1);
    }
}
