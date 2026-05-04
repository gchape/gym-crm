package tech.provokedynamic.gymcrm.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record Address(
        @NotBlank(message = "Street is required")
        String street,

        @NotBlank(message = "City is required")
        String city,

        @NotBlank(message = "Country is required")
        String country,

        @Pattern(regexp = "\\d{4,10}", message = "Invalid postal code")
        String postalCode
) {
}