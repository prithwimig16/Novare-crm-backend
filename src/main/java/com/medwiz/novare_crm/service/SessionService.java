package com.medwiz.novare_crm.service;
import com.medwiz.novare_crm.entity.UserSession;
import com.medwiz.novare_crm.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final UserSessionRepository sessionRepo;

    // 🔑 Use String userId (Keycloak "sub"), not UUID
    public String createSession(String userId) {
        String sessionId = UUID.randomUUID().toString();

        UserSession session = UserSession.builder()
                .userId(userId)
                .sessionId(sessionId)
                .createdAt(LocalDateTime.now())
                .lastActiveAt(LocalDateTime.now())
                .build();

        sessionRepo.save(session);
        return sessionId;
    }

    public String createOrUpdateSession(String userId) {
        String sessionId = UUID.randomUUID().toString();

        sessionRepo.findByUserId(userId).ifPresent(existing -> {
            existing.setSessionId(sessionId);
            existing.setLastActiveAt(LocalDateTime.now());
            sessionRepo.save(existing);
        });

        if (sessionRepo.findByUserId(userId).isEmpty()) {
            sessionRepo.save(UserSession.builder()
                    .userId(userId)
                    .sessionId(sessionId)
                    .createdAt(LocalDateTime.now())
                    .lastActiveAt(LocalDateTime.now())
                    .build());
        }

        return sessionId;
    }

    public boolean isValid(String userId, String sessionId) {
        return sessionRepo.findByUserIdAndSessionId(userId, sessionId).isPresent();
    }

    // ✅ Option 1: Invalidate specific session
    public void invalidateSession(String userId, String sessionId) {
        sessionRepo.deleteByUserIdAndSessionId(userId, sessionId);
    }

    // ✅ Option 2: Global logout (all sessions for user)
    public void invalidateAllSessions(String userId) {
        sessionRepo.deleteByUserId(userId);
    }

    // Update activity
    public void updateLastActive(String userId, String sessionId) {
        sessionRepo.findByUserIdAndSessionId(userId, sessionId).ifPresent(session -> {
            session.setLastActiveAt(LocalDateTime.now());
            sessionRepo.save(session);
        });
    }
}

