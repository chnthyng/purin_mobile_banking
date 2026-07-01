package me.purin.purin_mobile_banking.dto.response;

import me.purin.purin_mobile_banking.enums.MfaType;

import java.time.Instant;
import java.util.UUID;

public record UserMfaResponseDto(
        UUID mfaId,
        UUID userId,
        MfaType mfaType,
        boolean isVerified,
        Instant createdAt
) {
}