package me.purin.purin_mobile_banking.service;

import me.purin.purin_mobile_banking.TestDataFactory;
import me.purin.purin_mobile_banking.dto.request.TransactionCreateRequestDto;
import me.purin.purin_mobile_banking.dto.response.TransactionResponseDto;
import me.purin.purin_mobile_banking.entity.BankAccount;
import me.purin.purin_mobile_banking.entity.LedgerEntry;
import me.purin.purin_mobile_banking.entity.Transaction;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService")
class TransactionServiceTest {

    @Mock TransactionRepository transactionRepository;
    @Mock BankAccountRepository bankAccountRepository;
    @Mock LedgerEntryRepository ledgerEntryRepository;
    @Mock TransactionMapper transactionMapper;

    @InjectMocks TransactionService transactionService;

    @Nested
    @DisplayName("transfer: happy path")
    class TransferHappyPath {

        @Test
        @DisplayName("โอนสำเร็จ — balance ถูกหักและเพิ่มอย่างถูกต้อง")
        void transfer_success_balancesUpdated() {
            BigDecimal initialBalance = new BigDecimal("10000.00");
            BigDecimal amount = new BigDecimal("1000.00");

            BankAccount from = TestDataFactory.buildActiveAccount(
                    TestDataFactory.ACCOUNT_ID, TestDataFactory.USER_ID, initialBalance);
            BankAccount to = TestDataFactory.buildActiveAccount(
                    TestDataFactory.ACCOUNT_ID_2, TestDataFactory.USER_ID_2, new BigDecimal("5000.00"));
            Transaction savedTx = TestDataFactory.buildCompletedTransaction();
            TransactionResponseDto dto = TestDataFactory.buildTransactionResponseDto();
            TransactionCreateRequestDto request = TestDataFactory.buildTransferRequest(
                    TestDataFactory.ACCOUNT_ID, TestDataFactory.ACCOUNT_ID_2, amount);

            when(transactionRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
            when(bankAccountRepository.findById(TestDataFactory.ACCOUNT_ID)).thenReturn(Optional.of(from));
            when(bankAccountRepository.findById(TestDataFactory.ACCOUNT_ID_2)).thenReturn(Optional.of(to));
            when(bankAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(transactionRepository.save(any())).thenReturn(savedTx);
            when(transactionMapper.toResponseDto(savedTx)).thenReturn(dto);

            transactionService.transfer(TestDataFactory.USER_ID, request);

            assertThat(from.getBalance()).isEqualByComparingTo(new BigDecimal("9000.00"));
            assertThat(from.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("9000.00"));
            assertThat(to.getBalance()).isEqualByComparingTo(new BigDecimal("6000.00"));
        }

        @Test
        @DisplayName("โอนสำเร็จ — สร้าง ledger entry DEBIT และ CREDIT ครบ 2 รายการ")
        void transfer_success_ledgerEntriesCreated() {
            BigDecimal amount = new BigDecimal("500.00");
            BankAccount from = TestDataFactory.buildActiveAccount(
                    TestDataFactory.ACCOUNT_ID, TestDataFactory.USER_ID, new BigDecimal("10000.00"));
            BankAccount to = TestDataFactory.buildActiveAccount(
                    TestDataFactory.ACCOUNT_ID_2, TestDataFactory.USER_ID_2, new BigDecimal("5000.00"));
            Transaction savedTx = TestDataFactory.buildCompletedTransaction();
            TransactionCreateRequestDto request = TestDataFactory.buildTransferRequest(
                    TestDataFactory.ACCOUNT_ID, TestDataFactory.ACCOUNT_ID_2, amount);

            when(transactionRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
            when(bankAccountRepository.findById(TestDataFactory.ACCOUNT_ID)).thenReturn(Optional.of(from));
            when(bankAccountRepository.findById(TestDataFactory.ACCOUNT_ID_2)).thenReturn(Optional.of(to));
            when(bankAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(transactionRepository.save(any())).thenReturn(savedTx);
            when(transactionMapper.toResponseDto(any())).thenReturn(TestDataFactory.buildTransactionResponseDto());

            transactionService.transfer(TestDataFactory.USER_ID, request);

            var ledgerCaptor = ArgumentCaptor.forClass(LedgerEntry.class);
            verify(ledgerEntryRepository, times(2)).save(ledgerCaptor.capture());

            List<LedgerEntry> entries = ledgerCaptor.getAllValues();
            assertThat(entries).hasSize(2);

            LedgerEntry debit  = entries.stream().filter(e -> e.getEntryType() == EntryType.DEBIT).findFirst().orElseThrow();
            LedgerEntry credit = entries.stream().filter(e -> e.getEntryType() == EntryType.CREDIT).findFirst().orElseThrow();

            assertThat(debit.getAmount()).isEqualByComparingTo(credit.getAmount());
            assertThat(debit.getAccountId()).isEqualTo(TestDataFactory.ACCOUNT_ID);
            assertThat(credit.getAccountId()).isEqualTo(TestDataFactory.ACCOUNT_ID_2);
        }

        @Test
        @DisplayName("โอนสำเร็จ — transaction status เป็น COMPLETED และมี completedAt")
        void transfer_success_transactionStatusCompleted() {
            BankAccount from = TestDataFactory.buildActiveAccount(
                    TestDataFactory.ACCOUNT_ID, TestDataFactory.USER_ID, new BigDecimal("10000.00"));
            BankAccount to = TestDataFactory.buildActiveAccount(
                    TestDataFactory.ACCOUNT_ID_2, TestDataFactory.USER_ID_2, new BigDecimal("5000.00"));
            TransactionCreateRequestDto request = TestDataFactory.buildTransferRequest(
                    TestDataFactory.ACCOUNT_ID, TestDataFactory.ACCOUNT_ID_2, new BigDecimal("500.00"));

            when(transactionRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
            when(bankAccountRepository.findById(TestDataFactory.ACCOUNT_ID)).thenReturn(Optional.of(from));
            when(bankAccountRepository.findById(TestDataFactory.ACCOUNT_ID_2)).thenReturn(Optional.of(to));
            when(bankAccountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(transactionMapper.toResponseDto(any())).thenReturn(TestDataFactory.buildTransactionResponseDto());

            var txCaptor = ArgumentCaptor.forClass(Transaction.class);
            when(transactionRepository.save(txCaptor.capture())).thenAnswer(inv -> {
                Transaction t = inv.getArgument(0);
                t.setTransactionId(TestDataFactory.TX_ID);
                return t;
            });

            transactionService.transfer(TestDataFactory.USER_ID, request);

            Transaction captured = txCaptor.getValue();
            assertThat(captured.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
            assertThat(captured.getCompletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("transfer: validation failures")
    class TransferValidation {

        @Test
        @DisplayName("idempotency key ซ้ำ — throw DuplicateRequestException")
        void transfer_duplicateIdempotencyKey_throws() {
            Transaction existing = TestDataFactory.buildCompletedTransaction();
            TransactionCreateRequestDto request = TestDataFactory.buildTransferRequest(
                    TestDataFactory.ACCOUNT_ID, TestDataFactory.ACCOUNT_ID_2, new BigDecimal("500.00"));

            when(transactionRepository.findByIdempotencyKey(request.idempotencyKey()))
                    .thenReturn(Optional.of(existing));
            when(transactionMapper.toResponseDto(existing))
                    .thenReturn(TestDataFactory.buildTransactionResponseDto());

            assertThatThrownBy(() -> transactionService.transfer(TestDataFactory.USER_ID, request))
                    .isInstanceOf(DuplicatedRequestException.class)
                    .hasMessageContaining("Duplicate request");

            verify(bankAccountRepository, never()).findById(any());
            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("transactionType ไม่ใช่ TRANSFER — throw InvalidTransactionException")
        void transfer_unsupportedType_throws() {
            TransactionCreateRequestDto request = new TransactionCreateRequestDto(
                    TestDataFactory.ACCOUNT_ID, TestDataFactory.ACCOUNT_ID_2,
                    TransactionType.PAYMENT, new BigDecimal("500.00"), "THB", null, "idem-key-unsupported");

            when(transactionRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.transfer(TestDataFactory.USER_ID, request))
                    .isInstanceOf(InvalidTransactionException.class)
                    .hasMessageContaining("Unsupported transactionType");
        }

        @Test
        @DisplayName("fromAccountId == toAccountId — throw InvalidTransactionException")
        void transfer_sameAccount_throws() {
            TransactionCreateRequestDto request = TestDataFactory.buildTransferRequest(
                    TestDataFactory.ACCOUNT_ID, TestDataFactory.ACCOUNT_ID, new BigDecimal("500.00"));

            when(transactionRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.transfer(TestDataFactory.USER_ID, request))
                    .isInstanceOf(InvalidTransactionException.class)
                    .hasMessageContaining("same account");
        }

        @Test
        @DisplayName("fromAccount ไม่พบ — throw ResourceNotFoundException")
        void transfer_fromAccountNotFound_throws() {
            TransactionCreateRequestDto request = TestDataFactory.buildTransferRequest(
                    TestDataFactory.ACCOUNT_ID, TestDataFactory.ACCOUNT_ID_2, new BigDecimal("500.00"));

            when(transactionRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
            when(bankAccountRepository.findById(TestDataFactory.ACCOUNT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.transfer(TestDataFactory.USER_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Source account not found");
        }

        @Test
        @DisplayName("toAccount ไม่พบ — throw ResourceNotFoundException")
        void transfer_toAccountNotFound_throws() {
            BankAccount from = TestDataFactory.buildActiveAccount(
                    TestDataFactory.ACCOUNT_ID, TestDataFactory.USER_ID, new BigDecimal("10000.00"));
            TransactionCreateRequestDto request = TestDataFactory.buildTransferRequest(
                    TestDataFactory.ACCOUNT_ID, TestDataFactory.ACCOUNT_ID_2, new BigDecimal("500.00"));

            when(transactionRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
            when(bankAccountRepository.findById(TestDataFactory.ACCOUNT_ID)).thenReturn(Optional.of(from));
            when(bankAccountRepository.findById(TestDataFactory.ACCOUNT_ID_2)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.transfer(TestDataFactory.USER_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Destination account not found");
        }

        @Test
        @DisplayName("fromAccount FROZEN — throw InvalidTransactionException")
        void transfer_frozenFromAccount_throws() {
            BankAccount from = TestDataFactory.buildFrozenAccount(
                    TestDataFactory.ACCOUNT_ID, TestDataFactory.USER_ID);
            BankAccount to = TestDataFactory.buildActiveAccount(
                    TestDataFactory.ACCOUNT_ID_2, TestDataFactory.USER_ID_2, new BigDecimal("5000.00"));
            TransactionCreateRequestDto request = TestDataFactory.buildTransferRequest(
                    TestDataFactory.ACCOUNT_ID, TestDataFactory.ACCOUNT_ID_2, new BigDecimal("500.00"));

            when(transactionRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
            when(bankAccountRepository.findById(TestDataFactory.ACCOUNT_ID)).thenReturn(Optional.of(from));
            when(bankAccountRepository.findById(TestDataFactory.ACCOUNT_ID_2)).thenReturn(Optional.of(to));

            assertThatThrownBy(() -> transactionService.transfer(TestDataFactory.USER_ID, request))
                    .isInstanceOf(InvalidTransactionException.class)
                    .hasMessageContaining("not active");
        }

        @Test
        @DisplayName("ยอดไม่พอ — throw InvalidTransactionException")
        void transfer_insufficientBalance_throws() {
            BankAccount from = TestDataFactory.buildActiveAccount(
                    TestDataFactory.ACCOUNT_ID, TestDataFactory.USER_ID, new BigDecimal("100.00"));
            BankAccount to = TestDataFactory.buildActiveAccount(
                    TestDataFactory.ACCOUNT_ID_2, TestDataFactory.USER_ID_2, new BigDecimal("5000.00"));
            TransactionCreateRequestDto request = TestDataFactory.buildTransferRequest(
                    TestDataFactory.ACCOUNT_ID, TestDataFactory.ACCOUNT_ID_2, new BigDecimal("5000.00"));

            when(transactionRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
            when(bankAccountRepository.findById(TestDataFactory.ACCOUNT_ID)).thenReturn(Optional.of(from));
            when(bankAccountRepository.findById(TestDataFactory.ACCOUNT_ID_2)).thenReturn(Optional.of(to));

            assertThatThrownBy(() -> transactionService.transfer(TestDataFactory.USER_ID, request))
                    .isInstanceOf(InvalidTransactionException.class)
                    .hasMessageContaining("Insufficient available balance");

            verify(transactionRepository, never()).save(any());
            verify(ledgerEntryRepository, never()).save(any());
        }

        @Test
        @DisplayName("optimistic lock fail — throw InvalidTransactionException (แจ้ง retry)")
        void transfer_optimisticLockFailure_throws() {
            BankAccount from = TestDataFactory.buildActiveAccount(
                    TestDataFactory.ACCOUNT_ID, TestDataFactory.USER_ID, new BigDecimal("10000.00"));
            BankAccount to = TestDataFactory.buildActiveAccount(
                    TestDataFactory.ACCOUNT_ID_2, TestDataFactory.USER_ID_2, new BigDecimal("5000.00"));
            TransactionCreateRequestDto request = TestDataFactory.buildTransferRequest(
                    TestDataFactory.ACCOUNT_ID, TestDataFactory.ACCOUNT_ID_2, new BigDecimal("500.00"));

            when(transactionRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
            when(bankAccountRepository.findById(TestDataFactory.ACCOUNT_ID)).thenReturn(Optional.of(from));
            when(bankAccountRepository.findById(TestDataFactory.ACCOUNT_ID_2)).thenReturn(Optional.of(to));
            when(bankAccountRepository.save(any())).thenThrow(new OptimisticLockingFailureException("version conflict"));

            assertThatThrownBy(() -> transactionService.transfer(TestDataFactory.USER_ID, request))
                    .isInstanceOf(InvalidTransactionException.class)
                    .hasMessageContaining("retry");
        }
    }

    @Test
    @DisplayName("getById: พบ transaction — return DTO")
    void getById_found() {
        Transaction tx = TestDataFactory.buildCompletedTransaction();
        TransactionResponseDto dto = TestDataFactory.buildTransactionResponseDto();
        when(transactionRepository.findById(TestDataFactory.TX_ID)).thenReturn(Optional.of(tx));
        when(transactionMapper.toResponseDto(tx)).thenReturn(dto);

        TransactionResponseDto result = transactionService.getById(TestDataFactory.TX_ID);
        assertThat(result.transactionId()).isEqualTo(TestDataFactory.TX_ID);
    }

    @Test
    @DisplayName("getById: ไม่พบ transaction — throw ResourceNotFoundException")
    void getById_notFound() {
        when(transactionRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> transactionService.getById(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transaction not found");
    }
}