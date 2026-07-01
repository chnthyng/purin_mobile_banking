package me.purin.purin_mobile_banking.repository;

import me.purin.purin_mobile_banking.entity.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserCredentialRepository extends JpaRepository<UserCredential, UUID> {

    Optional<UserCredential> findByUserId(UUID userId);
}