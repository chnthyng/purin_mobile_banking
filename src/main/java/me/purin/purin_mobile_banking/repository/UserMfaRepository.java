package me.purin.purin_mobile_banking.repository;

import me.purin.purin_mobile_banking.entity.UserMfa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserMfaRepository extends JpaRepository<UserMfa, UUID> {

    List<UserMfa> findByUserId(UUID userId);

    List<UserMfa> findByUserIdAndIsVerifiedTrue(UUID userId);
}