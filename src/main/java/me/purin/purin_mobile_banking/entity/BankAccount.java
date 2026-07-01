package me.purin.purin_mobile_banking.entity;

import jakarta.persistence.*;
import lombok.*;
import me.purin.purin_mobile_banking.enums.AccountStatus;
import me.purin.purin_mobile_banking.enums.AccountType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bank_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "account_id", updatable = false, nullable = false)
    private UUID accountId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "account_number", length = 34, nullable = false)
    private String accountNumber;

    @Column(name = "account_number_hash", length = 255, nullable = false)
    private String accountNumberHash;

    @Column(name = "bank_code", length = 10, nullable = false)
    private String bankCode;

    @Column(name = "bank_name", length = 100, nullable = false)
    private String bankName;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", length = 20, nullable = false)
    @Builder.Default
    private AccountType accountType = AccountType.SAVINGS;

    @Column(name = "currency", length = 3, nullable = false)
    @Builder.Default
    private String currency = "THB";

    @Column(name = "balance", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "available_balance", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private boolean isPrimary = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private long version = 0L;
}