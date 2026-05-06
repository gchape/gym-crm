package tech.provokedynamic.gymcrm.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import tech.provokedynamic.gymcrm.model.Address;

import java.time.LocalDate;

@Getter
@SuperBuilder
public final class Trainee extends User implements Entity {
    private final long id;
    private final Address address;
    private final LocalDate dateOfBirth;

    @JsonCreator
    public static Trainee of(
            @JsonProperty("id") long id,
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("username") String username,
            @JsonProperty("password") String password,
            @JsonProperty("isActive") boolean isActive,
            @JsonProperty("address") Address address,
            @JsonProperty("dateOfBirth") LocalDate dateOfBirth
    ) {
        return Trainee.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .password(password)
                .isActive(isActive)
                .address(address)
                .dateOfBirth(dateOfBirth)
                .build();
    }
}
