package tech.provokedynamic.gymcrm.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoginAttemptService")
class LoginAttemptServiceTest {

    LoginAttemptService service;

    @BeforeEach
    void setUp() {
        service = new LoginAttemptService();
    }

    @Nested
    @DisplayName("isBlocked()")
    class IsBlocked {

        @Test
        @DisplayName("not blocked with no attempts recorded")
        void notBlockedInitially() {
            assertThat(service.isBlocked("user")).isFalse();
        }

        @Test
        @DisplayName("not blocked after fewer than 3 failures")
        void notBlockedBeforeThreshold() {
            service.onFailure("user");
            service.onFailure("user");

            assertThat(service.isBlocked("user")).isFalse();
        }

        @Test
        @DisplayName("blocked after exactly 3 failures")
        void blockedAfterThreeFailures() {
            service.onFailure("user");
            service.onFailure("user");
            service.onFailure("user");

            assertThat(service.isBlocked("user")).isTrue();
        }

        @Test
        @DisplayName("blocked after more than 3 failures")
        void blockedAfterMoreThanThreeFailures() {
            service.onFailure("user");
            service.onFailure("user");
            service.onFailure("user");
            service.onFailure("user");

            assertThat(service.isBlocked("user")).isTrue();
        }

        @Test
        @DisplayName("different users are tracked independently")
        void independentTrackingPerUser() {
            service.onFailure("userA");
            service.onFailure("userA");
            service.onFailure("userA");

            assertThat(service.isBlocked("userA")).isTrue();
            assertThat(service.isBlocked("userB")).isFalse();
        }
    }

    @Nested
    @DisplayName("onSuccess()")
    class OnSuccess {

        @Test
        @DisplayName("clears failure count so user is no longer blocked")
        void successClearsBlock() {
            service.onFailure("user");
            service.onFailure("user");
            service.onFailure("user");

            assertThat(service.isBlocked("user")).isTrue();

            service.onSuccess("user");

            assertThat(service.isBlocked("user")).isFalse();
        }

        @Test
        @DisplayName("success on unknown user does not throw")
        void successOnUnknownUserIsNoop() {
            service.onSuccess("nonexistent");

            assertThat(service.isBlocked("nonexistent")).isFalse();
        }
    }
}
