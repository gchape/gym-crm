package tech.provokedynamic.gymcrm.entity;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class User {
    private final String firstName;
    private final String lastName;
    private final String username;
    private final String password;
    private final boolean isActive;
}
