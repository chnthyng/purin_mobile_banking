package me.purin.purin_mobile_banking.dto.response;

import me.purin.purin_mobile_banking.enums.EntryType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LedgerEntryResponseDto(
        UUID entryId,
        UUID transactionId,
        UUID accountId,
        EntryType entryType,
        BigDecimal amount,
        Instant createdAt
) {
}
