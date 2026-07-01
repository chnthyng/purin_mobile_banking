package me.purin.purin_mobile_banking.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRegisterRequestDto(

        @NotBlank
        @Size(min = 3, max = 50)
        String username,

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @Pattern(regexp = "^[0-9+\\-\\s]{9,20}$", message = "Invalid phone number format")
        String phoneNumber,

        @NotBlank
        @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
        String password
) {
}