package me.purin.purin_mobile_banking.entity;

import jakarta.persistence.*;
import lombok.*;
import me.purin.purin_mobile_banking.enums.EntryType;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "entry_id", updatable = false, nullable = false)
    private UUID entryId;

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", length = 10, nullable = false)
    private EntryType entryType;

    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}