package me.purin.purin_mobile_banking.mapper;

import me.purin.purin_mobile_banking.dto.response.TransactionResponseDto;
import me.purin.purin_mobile_banking.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponseDto toResponseDto(Transaction tx) {
        return new TransactionResponseDto(
                tx.getTransactionId(),
                tx.getReferenceNo(),
                tx.getFromAccountId(),
                tx.getToAccountId(),
                tx.getTransactionType(),
                tx.getAmount(),
                tx.getCurrency(),
                tx.getFee(),
                tx.getStatus(),
                tx.getBalanceBefore(),
                tx.getBalanceAfter(),
                tx.getDescription(),
                tx.getInitiatedBy(),
                tx.getCreatedAt(),
                tx.getCompletedAt(),
                tx.getMetadata()
        );
    }
}
