package me.purin.purin_mobile_banking.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UserResponseDto(
        UUID userId,
        String username,
        String email,
        String phoneNumber,
        boolean isActive,
        Instant createdAt
) {
}