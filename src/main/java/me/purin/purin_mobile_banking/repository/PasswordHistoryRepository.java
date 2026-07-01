package me.purin.purin_mobile_banking.repository;

import me.purin.purin_mobile_banking.entity.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, UUID> {

    List<PasswordHistory> findByUserIdOrderByCreatedAtDesc(UUID userId);
}