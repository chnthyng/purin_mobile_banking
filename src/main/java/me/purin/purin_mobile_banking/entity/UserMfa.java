package me.purin.purin_mobile_banking.entity;

import jakarta.persistence.*;
import lombok.*;
import me.purin.purin_mobile_banking.enums.MfaType;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_mfa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMfa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "mfa_id", updatable = false, nullable = false)
    private UUID mfaId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "mfa_type", length = 20, nullable = false)
    private MfaType mfaType;

    @Column(name = "secret_encrypted", columnDefinition = "text")
    private String secretEncrypted;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean isVerified = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}