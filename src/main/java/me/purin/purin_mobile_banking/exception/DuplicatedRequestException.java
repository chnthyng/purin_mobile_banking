package me.purin.purin_mobile_banking.exception;

import lombok.Getter;
import me.purin.purin_mobile_banking.dto.response.TransactionResponseDto;

@Getter
public class DuplicatedRequestException extends RuntimeException {

    private final TransactionResponseDto existingTransaction;

    public DuplicatedRequestException(String message, TransactionResponseDto existingTransaction) {
        super(message);
        this.existingTransaction = existingTransaction;
    }
}