package tech.provokedynamic.gymcrm.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.provokedynamic.gymcrm.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialGeneratorTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CredentialGenerator credentialGenerator;

    @Test
    void generatePassword_returnsExactlyTenCharacters() {
        var password = credentialGenerator.generatePassword();

        assertThat(password).hasSize(10);
    }

    @Test
    void generatePassword_returnsDifferentValuesOnSuccessiveCalls() {
        var first = credentialGenerator.generatePassword();
        var second = credentialGenerator.generatePassword();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void generateUsername_returnsBaseName_whenNotTaken() {
        var firstName = "John";
        var lastName = "Doe";
        var base = firstName + "." + lastName;

        when(userRepository.existsByUsernameIncludingDeleted(base)).thenReturn(false);

        var result = credentialGenerator.generateUsername(firstName, lastName);

        assertThat(result).isEqualTo(base);
    }

    @Test
    void generateUsername_appendsOne_whenBaseNameTaken() {
        var firstName = "John";
        var lastName = "Doe";
        var base = firstName + "." + lastName;

        when(userRepository.existsByUsernameIncludingDeleted(base)).thenReturn(true);
        when(userRepository.existsByUsernameIncludingDeleted(base + "1")).thenReturn(false);

        var result = credentialGenerator.generateUsername(firstName, lastName);

        assertThat(result).isEqualTo(base + "1");
    }

    @Test
    void generateUsername_incrementsSuffix_whenMultipleNamesTaken() {
        var firstName = "John";
        var lastName = "Doe";
        var base = firstName + "." + lastName;

        when(userRepository.existsByUsernameIncludingDeleted(base)).thenReturn(true);
        when(userRepository.existsByUsernameIncludingDeleted(base + "1")).thenReturn(true);
        when(userRepository.existsByUsernameIncludingDeleted(base + "2")).thenReturn(false);

        var result = credentialGenerator.generateUsername(firstName, lastName);

        assertThat(result).isEqualTo(base + "2");
    }
}
