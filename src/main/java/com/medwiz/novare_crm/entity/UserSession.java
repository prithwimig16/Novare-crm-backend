package com.medwiz.novare_crm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {

    @Id
    @GeneratedValue
    private UUID id;

    // Store Keycloak userId (sub) as String, not UUID
    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, unique = true)
    private String sessionId;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime lastActiveAt = LocalDateTime.now();
}


