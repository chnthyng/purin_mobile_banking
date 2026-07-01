package me.purin.purin_mobile_banking.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import me.purin.purin_mobile_banking.enums.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionCreateRequestDto(

        UUID fromAccountId,

        UUID toAccountId,

        @NotNull
        TransactionType transactionType,

        @NotNull
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,

        @Size(min = 3, max = 3)
        String currency,

        @Size(max = 255)
        String description,

        @NotNull
        @Size(min = 1, max = 100)
        String idempotencyKey
) {
}
