package me.purin.purin_mobile_banking.repository;

import me.purin.purin_mobile_banking.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, UUID> {

    List<LoginAttempt> findByUserIdOrderByAttemptedAtDesc(UUID userId);

    List<LoginAttempt> findByUserIdAndIsSuccessfulFalseAndAttemptedAtAfter(
            UUID userId, Instant since);
}