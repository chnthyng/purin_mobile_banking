package me.purin.purin_mobile_banking.repository;

import me.purin.purin_mobile_banking.entity.Transaction;
import me.purin.purin_mobile_banking.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByReferenceNo(String referenceNo);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT t FROM Transaction t WHERE t.fromAccountId = :accountId "
            + "OR t.toAccountId = :accountId ORDER BY t.createdAt DESC")
    List<Transaction> findByAccountId(@Param("accountId") UUID accountId);

    List<Transaction> findByStatus(TransactionStatus status);
}
