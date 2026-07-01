package me.purin.purin_mobile_banking.dto.response;

import me.purin.purin_mobile_banking.enums.AccountStatus;
import me.purin.purin_mobile_banking.enums.AccountType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BankAccountResponseDto(
        UUID accountId,
        UUID userId,
        String maskedAccountNumber,
        String bankCode,
        String bankName,
        AccountType accountType,
        String currency,
        BigDecimal balance,
        BigDecimal availableBalance,
        AccountStatus status,
        boolean isPrimary,
        Instant createdAt
) {
}