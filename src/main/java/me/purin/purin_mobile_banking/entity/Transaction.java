package me.purin.purin_mobile_banking.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import me.purin.purin_mobile_banking.enums.TransactionStatus;
import me.purin.purin_mobile_banking.enums.TransactionType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "transaction_id", updatable = false, nullable = false)
    private UUID transactionId;

    @Column(name = "reference_no", length = 50, nullable = false, unique = true)
    private String referenceNo;

    @Column(name = "from_account_id")
    private UUID fromAccountId;

    @Column(name = "to_account_id")
    private UUID toAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 30, nullable = false)
    private TransactionType transactionType;

    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", length = 3, nullable = false)
    @Builder.Default
    private String currency = "THB";

    @Column(name = "fee", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal fee = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "balance_before", precision = 18, scale = 2)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", precision = 18, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "initiated_by", nullable = false)
    private UUID initiatedBy;

    @Column(name = "idempotency_key", length = 100, unique = true)
    private String idempotencyKey;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Type(JsonType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;
}