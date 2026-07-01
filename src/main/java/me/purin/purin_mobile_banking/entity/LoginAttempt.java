package me.purin.purin_mobile_banking.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "login_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "attempt_id", updatable = false, nullable = false)
    private UUID attemptId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "ip_address", columnDefinition = "inet", nullable = false)
    private String ipAddress;

    @Column(name = "device_info", columnDefinition = "text")
    private String deviceInfo;

    @Column(name = "is_successful", nullable = false)
    private boolean isSuccessful;

    @CreationTimestamp
    @Column(name = "attempted_at", nullable = false, updatable = false)
    private Instant attemptedAt;
}