package com.medwiz.novare_crm.repository;

import com.medwiz.novare_crm.entity.RetreatRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface RetreatRegistrationRepository extends JpaRepository<RetreatRegistration, UUID>, JpaSpecificationExecutor<RetreatRegistration> {

    // Find all registrations for a given member
    List<RetreatRegistration> findByMember_KeycloakUserId(String memberId);

    // Check if a member already registered for a retreat with same goal/mode (optional business rule)
    boolean existsByMember_KeycloakUserIdAndGoal(String memberId, Enum goal);

    @EntityGraph(attributePaths = {"member", "member.user"})
    Page<RetreatRegistration> findAll(Specification<RetreatRegistration> spec, Pageable pageable);

}
