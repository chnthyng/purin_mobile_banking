package me.purin.purin_mobile_banking.repository;

import me.purin.purin_mobile_banking.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankAccountRepository extends JpaRepository<BankAccount, UUID> {

    List<BankAccount> findByUserId(UUID userId);

    Optional<BankAccount> findByUserIdAndIsPrimaryTrue(UUID userId);

    Optional<BankAccount> findByAccountNumberHashAndBankCode(
            String accountNumberHash, String bankCode);
}
