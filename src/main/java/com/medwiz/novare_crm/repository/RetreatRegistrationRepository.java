package com.medwiz.novare_crm.repository;

import com.medwiz.novare_crm.entity.RetreatRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RetreatRegistrationRepository extends JpaRepository<RetreatRegistration, Long> {

    // Find all registrations for a given member
    List<RetreatRegistration> findByMember_KeycloakUserId(String memberId);

    // Check if a member already registered for a retreat with same goal/mode (optional business rule)
    boolean existsByMember_KeycloakUserIdAndGoal(String memberId, Enum goal);
}
