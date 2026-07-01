package me.purin.purin_mobile_banking.service;

import lombok.RequiredArgsConstructor;
import me.purin.purin_mobile_banking.dto.request.TransactionCreateRequestDto;
import me.purin.purin_mobile_banking.dto.response.TransactionResponseDto;
import me.purin.purin_mobile_banking.entity.BankAccount;
import me.purin.purin_mobile_banking.entity.LedgerEntry;
import me.purin.purin_mobile_banking.entity.Transaction;
import me.purin.purin_mobile_banking.enums.AccountStatus;
import me.purin.purin_mobile_banking.enums.EntryType;
import me.purin.purin_mobile_banking.enums.TransactionStatus;
import me.purin.purin_mobile_banking.enums.TransactionType;
import me.purin.purin_mobile_banking.exception.DuplicatedRequestException;
import me.purin.purin_mobile_banking.exception.InvalidTransactionException;
import me.purin.purin_mobile_banking.exception.ResourceNotFoundException;
import me.purin.purin_mobile_banking.mapper.TransactionMapper;
import me.purin.purin_mobile_banking.repository.BankAccountRepository;
import me.purin.purin_mobile_banking.repository.LedgerEntryRepository;
import me.purin.purin_mobile_banking.repository.TransactionRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final TransactionMapper transactionMapper;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransactionResponseDto transfer(UUID initiatedBy, TransactionCreateRequestDto request) {

        transactionRepository.findByIdempotencyKey(request.idempotencyKey())
                .ifPresent(existing -> {
                    throw new DuplicatedRequestException(
                            "Duplicate request for idempotencyKey: " + request.idempotencyKey(),
                            transactionMapper.toResponseDto(existing));
                });

        if (request.transactionType() == TransactionType.TRANSFER) {
            return executeTransfer(initiatedBy, request);
        }
        throw new InvalidTransactionException(
                "Unsupported transactionType for this endpoint: " + request.transactionType());
    }

    private TransactionResponseDto executeTransfer(UUID initiatedBy, TransactionCreateRequestDto request) {
        if (request.fromAccountId() == null || request.toAccountId() == null) {
            throw new InvalidTransactionException("Transfer requires both fromAccountId and toAccountId");
        }
        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new InvalidTransactionException("Cannot transfer to the same account");
        }

        BankAccount fromAccount = bankAccountRepository.findById(request.fromAccountId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Source account not found: " + request.fromAccountId()));
        BankAccount toAccount = bankAccountRepository.findById(request.toAccountId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Destination account not found: " + request.toAccountId()));

        validateAccountIsTransactable(fromAccount, "Source");
        validateAccountIsTransactable(toAccount, "Destination");

        BigDecimal amount = request.amount();
        if (fromAccount.getAvailableBalance().compareTo(amount) < 0) {
            throw new InvalidTransactionException("Insufficient available balance in source account");
        }

        BigDecimal fromBalanceBefore = fromAccount.getBalance();
        BigDecimal toBalanceBefore = toAccount.getBalance();

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        fromAccount.setAvailableBalance(fromAccount.getAvailableBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));
        toAccount.setAvailableBalance(toAccount.getAvailableBalance().add(amount));

        try {
            bankAccountRepository.save(fromAccount);
            bankAccountRepository.save(toAccount);
        } catch (OptimisticLockingFailureException ex) {
            throw new InvalidTransactionException(
                    "Account balance changed concurrently — please retry the transfer");
        }

        Transaction transaction = Transaction.builder()
                .referenceNo(generateReferenceNo())
                .fromAccountId(fromAccount.getAccountId())
                .toAccountId(toAccount.getAccountId())
                .transactionType(TransactionType.TRANSFER)
                .amount(amount)
                .currency(request.currency() != null ? request.currency() : fromAccount.getCurrency())
                .status(TransactionStatus.COMPLETED)
                .balanceBefore(fromBalanceBefore)
                .balanceAfter(fromAccount.getBalance())
                .description(request.description())
                .initiatedBy(initiatedBy)
                .idempotencyKey(request.idempotencyKey())
                .completedAt(Instant.now())
                .build();
        transaction = transactionRepository.save(transaction);

        LedgerEntry debitEntry = LedgerEntry.builder()
                .transactionId(transaction.getTransactionId())
                .accountId(fromAccount.getAccountId())
                .entryType(EntryType.DEBIT)
                .amount(amount)
                .build();
        LedgerEntry creditEntry = LedgerEntry.builder()
                .transactionId(transaction.getTransactionId())
                .accountId(toAccount.getAccountId())
                .entryType(EntryType.CREDIT)
                .amount(amount)
                .build();
        ledgerEntryRepository.save(debitEntry);
        ledgerEntryRepository.save(creditEntry);

        return transactionMapper.toResponseDto(transaction);
    }

    private void validateAccountIsTransactable(BankAccount account, String role) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidTransactionException(
                    role + " account is not active (status: " + account.getStatus() + ")");
        }
    }

    @Transactional(readOnly = true)
    public TransactionResponseDto getById(UUID transactionId) {
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));
        return transactionMapper.toResponseDto(tx);
    }

    private String generateReferenceNo() {
        return "TXN-" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
