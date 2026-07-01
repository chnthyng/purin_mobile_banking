package me.purin.purin_mobile_banking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import me.purin.purin_mobile_banking.enums.AccountType;

public record BankAccountCreateRequestDto(

        @NotBlank
        @Size(max = 34)
        String accountNumber,

        @NotBlank
        @Size(max = 10)
        String bankCode,

        @NotBlank
        @Size(max = 100)
        String bankName,

        @NotNull
        AccountType accountType,

        @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
        String currency,

        boolean isPrimary
) {
}
