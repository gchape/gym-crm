package tech.provokedynamic.gymcrm.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import tech.provokedynamic.gymcrm.model.Specialization;

@Getter
@SuperBuilder
public final class Trainer extends User implements Entity {
    private final long id;
    private final Specialization specialization;

    @JsonCreator
    public static Trainer of(
            @JsonProperty("id") long id,
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("username") String username,
            @JsonProperty("password") String password,
            @JsonProperty("isActive") boolean isActive,
            @JsonProperty("specialization") Specialization specialization
    ) {
        return Trainer.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .password(password)
                .isActive(isActive)
                .specialization(specialization)
                .build();
    }
}
