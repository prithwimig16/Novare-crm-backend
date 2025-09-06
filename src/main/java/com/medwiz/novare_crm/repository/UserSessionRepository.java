package com.medwiz.novare_crm.repository;

import com.medwiz.novare_crm.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
    // Find all sessions for a user
    //List<UserSession> findByUserId(String userId);
    Optional<UserSession> findByUserId(String userId);


    // Find a specific session
    Optional<UserSession> findByUserIdAndSessionId(String userId, String sessionId);

    // Delete a specific session
    void deleteByUserIdAndSessionId(String userId, String sessionId);

    // Delete all sessions for a user (global logout)
    void deleteByUserId(String userId);



    // Cleanup old sessions
    void deleteByLastActiveAtBefore(LocalDateTime cutoff);
}

