package me.purin.purin_mobile_banking.dto.response;

import me.purin.purin_mobile_banking.enums.TransactionStatus;
import me.purin.purin_mobile_banking.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record TransactionResponseDto(
        UUID transactionId,
        String referenceNo,
        UUID fromAccountId,
        UUID toAccountId,
        TransactionType transactionType,
        BigDecimal amount,
        String currency,
        BigDecimal fee,
        TransactionStatus status,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        String description,
        UUID initiatedBy,
        Instant createdAt,
        Instant completedAt,
        Map<String, Object> metadata
) {
}