package me.purin.purin_mobile_banking.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_credentials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "credential_id", updatable = false, nullable = false)
    private UUID credentialId;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Column(name = "algorithm", length = 20, nullable = false)
    @Builder.Default
    private String algorithm = "argon2id";

    @CreationTimestamp
    @Column(name = "password_updated_at", nullable = false, updatable = false)
    private Instant passwordUpdatedAt;

    @Column(name = "must_change_password", nullable = false)
    @Builder.Default
    private boolean mustChangePassword = false;
}