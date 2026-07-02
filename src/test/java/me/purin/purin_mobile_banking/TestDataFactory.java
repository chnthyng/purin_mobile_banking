package me.purin.purin_mobile_banking;

import me.purin.purin_mobile_banking.dto.request.BankAccountCreateRequestDto;
import me.purin.purin_mobile_banking.dto.request.TransactionCreateRequestDto;
import me.purin.purin_mobile_banking.dto.request.UserRegisterRequestDto;
import me.purin.purin_mobile_banking.dto.response.TransactionResponseDto;
import me.purin.purin_mobile_banking.entity.BankAccount;
import me.purin.purin_mobile_banking.entity.Transaction;
import me.purin.purin_mobile_banking.entity.User;
import me.purin.purin_mobile_banking.enums.AccountStatus;
import me.purin.purin_mobile_banking.enums.AccountType;
import me.purin.purin_mobile_banking.enums.TransactionStatus;
import me.purin.purin_mobile_banking.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class TestDataFactory {

    public static final UUID USER_ID       = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID USER_ID_2     = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID ACCOUNT_ID    = UUID.fromString("aaaa0001-aaaa-0001-aaaa-000000000001");
    public static final UUID ACCOUNT_ID_2  = UUID.fromString("aaaa0002-aaaa-0002-aaaa-000000000002");
    public static final UUID TX_ID         = UUID.fromString("bbbb0001-bbbb-0001-bbbb-000000000001");

    private TestDataFactory() {}

    public static User buildUser() {
        User user = new User();
        user.setUserId(USER_ID);
        user.setUsername("somchai_p");
        user.setEmail("somchai.p@example.com");
        user.setPhoneNumber("+66812345671");
        user.setActive(true);
        user.setFailedLoginCount((short) 0);
        user.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        user.setUpdatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        return user;
    }

    public static UserRegisterRequestDto buildRegisterRequest() {
        return new UserRegisterRequestDto("somchai_p", "somchai.p@example.com", "+66812345671", "SecureP@ss123");
    }

    public static BankAccount buildActiveAccount(UUID accountId, UUID userId, BigDecimal balance) {
        BankAccount account = new BankAccount();
        account.setAccountId(accountId);
        account.setUserId(userId);
        account.setAccountNumber("0011122233341");
        account.setAccountNumberHash("HASH[0011122233341]");
        account.setBankCode("SCB");
        account.setBankName("Siam Commercial Bank");
        account.setAccountType(AccountType.SAVINGS);
        account.setCurrency("THB");
        account.setBalance(balance);
        account.setAvailableBalance(balance);
        account.setStatus(AccountStatus.ACTIVE);
        account.setPrimary(true);
        account.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        account.setUpdatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        return account;
    }

    public static BankAccount buildFrozenAccount(UUID accountId, UUID userId) {
        BankAccount account = buildActiveAccount(accountId, userId, BigDecimal.ZERO);
        account.setStatus(AccountStatus.FROZEN);
        return account;
    }

    public static BankAccountCreateRequestDto buildAccountCreateRequest() {
        return new BankAccountCreateRequestDto(
                "0011122233341", "SCB", "Siam Commercial Bank", AccountType.SAVINGS, "THB", true);
    }

    public static TransactionCreateRequestDto buildTransferRequest(
            UUID fromAccountId, UUID toAccountId, BigDecimal amount) {
        return new TransactionCreateRequestDto(
                fromAccountId, toAccountId, TransactionType.TRANSFER,
                amount, "THB", "Test transfer", "idem-key-test-0001");
    }

    public static Transaction buildCompletedTransaction() {
        Transaction tx = new Transaction();
        tx.setTransactionId(TX_ID);
        tx.setReferenceNo("TXN-1000000001");
        tx.setFromAccountId(ACCOUNT_ID);
        tx.setToAccountId(ACCOUNT_ID_2);
        tx.setTransactionType(TransactionType.TRANSFER);
        tx.setAmount(new BigDecimal("500.00"));
        tx.setCurrency("THB");
        tx.setFee(BigDecimal.ZERO);
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setBalanceBefore(new BigDecimal("10000.00"));
        tx.setBalanceAfter(new BigDecimal("9500.00"));
        tx.setInitiatedBy(USER_ID);
        tx.setIdempotencyKey("idem-key-test-0001");
        tx.setCreatedAt(Instant.parse("2024-06-01T10:00:00Z"));
        tx.setCompletedAt(Instant.parse("2024-06-01T10:00:01Z"));
        return tx;
    }

    public static TransactionResponseDto buildTransactionResponseDto() {
        Transaction tx = buildCompletedTransaction();
        return new TransactionResponseDto(
                tx.getTransactionId(), tx.getReferenceNo(),
                tx.getFromAccountId(), tx.getToAccountId(),
                tx.getTransactionType(), tx.getAmount(), tx.getCurrency(),
                tx.getFee(), tx.getStatus(), tx.getBalanceBefore(), tx.getBalanceAfter(),
                tx.getDescription(), tx.getInitiatedBy(),
                tx.getCreatedAt(), tx.getCompletedAt(), null);
    }
}