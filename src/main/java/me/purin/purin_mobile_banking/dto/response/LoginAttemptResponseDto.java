package me.purin.purin_mobile_banking.dto.response;

import java.time.Instant;
import java.util.UUID;

public record LoginAttemptResponseDto(
        UUID attemptId,
        String ipAddress,
        String deviceInfo,
        boolean isSuccessful,
        Instant attemptedAt
) {
}
