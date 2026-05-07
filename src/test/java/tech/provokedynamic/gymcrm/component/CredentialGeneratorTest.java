package tech.provokedynamic.gymcrm.component;

import org.junit.jupiter.api.Test;
import tech.provokedynamic.gymcrm.entity.Trainee;
import tech.provokedynamic.gymcrm.entity.User;
import tech.provokedynamic.gymcrm.model.Address;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CredentialGeneratorTest {

    private static final Address DUMMY_ADDRESS = new Address("Street", "City", "Country", "4600");

    CredentialGenerator credentialGenerator = new CredentialGenerator();

    private Trainee traineeWithUsername(String username) {
        return Trainee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .username(username)
                .password("password")
                .isActive(true)
                .address(DUMMY_ADDRESS)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void shouldGenerateUsernameWhenNoUsersExist() {
        List<User> users = List.of();

        String username = credentialGenerator.generateUsername("John", "Doe", users);

        assertThat(username).isEqualTo("John.Doe");
    }

    @Test
    void shouldAppendSuffixWhenUsernameAlreadyExists() {
        List<User> users = List.of(traineeWithUsername("John.Doe"));

        String username = credentialGenerator.generateUsername("John", "Doe", users);

        assertThat(username).isEqualTo("John.Doe1");
    }

    @Test
    void shouldIncrementSuffixWhenMultipleUsersExist() {
        List<User> users = List.of(
                traineeWithUsername("John.Doe"),
                traineeWithUsername("John.Doe1")
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
