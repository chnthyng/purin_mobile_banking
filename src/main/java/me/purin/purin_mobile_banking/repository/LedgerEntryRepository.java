package me.purin.purin_mobile_banking.repository;

import me.purin.purin_mobile_banking.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    List<LedgerEntry> findByTransactionId(UUID transactionId);

    List<LedgerEntry> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
}