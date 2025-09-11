package com.medwiz.novare_crm.service;

import com.medwiz.novare_crm.dto.request.RetreatRegistrationRequest;
import com.medwiz.novare_crm.dto.response.PaginatedResponse;
import com.medwiz.novare_crm.dto.response.RetreatRegistrationResponse;
import com.medwiz.novare_crm.entity.MemberProfile;
import com.medwiz.novare_crm.entity.RetreatRegistration;
import com.medwiz.novare_crm.entity.User;
import com.medwiz.novare_crm.enums.Gender;
import com.medwiz.novare_crm.enums.Goal;
import com.medwiz.novare_crm.enums.PreferredMode;
import com.medwiz.novare_crm.enums.Role;
import com.medwiz.novare_crm.exception.RegistrationException;
import com.medwiz.novare_crm.keycloak.KeycloakAdminService;
import com.medwiz.novare_crm.repository.MemberProfileRepository;
import com.medwiz.novare_crm.repository.RetreatRegistrationRepository;
import com.medwiz.novare_crm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetreatService {

    private final UserRepository userRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final RetreatRegistrationRepository retreatRegistrationRepository;
    private final KeycloakAdminService keycloakAdminService;

    /**
     * First-time registration: creates Keycloak user, User entity, MemberProfile, and RetreatRegistration
     */
    @Transactional
    public String registerMemberForFirstRetreat(RetreatRegistrationRequest request) {
        String keycloakUserId = null;
        try {
            // Create in Keycloak
            keycloakAdminService.createUser(
                    request.firstName(),
                    request.lastName(),
                    request.phoneNumber(),
                    request.email(),
                    request.password()
            );
            keycloakUserId = keycloakAdminService.getUserIdByUsername(request.phoneNumber());
            keycloakAdminService.assignRole(keycloakUserId, Role.DOCTOR);

            // Save User
            User user = saveUserEntity(keycloakUserId, request);
            // Save Profile
            MemberProfile profile = saveMemberProfile(keycloakUserId, request);
            // Save RetreatRegistration
            saveRetreatRegistration(profile, request);

            return "Member registered and retreat booked successfully";
        } catch (Exception ex) {
            rollbackOnFailure(request.phoneNumber(), keycloakUserId, ex);
            return null;
        }
    }

    /**
     * Existing member registers for another retreat
     */
    @Transactional
    public String registerForRetreat(String keycloakUserId, RetreatRegistrationRequest request) {
        MemberProfile profile = memberProfileRepository.findById(keycloakUserId)
                .orElseThrow(() -> new RegistrationException("Member profile not found"));

        saveRetreatRegistration(profile, request);
        return "Retreat registered successfully";
    }

    private User saveUserEntity(String keycloakUserId, RetreatRegistrationRequest request) {
        User user = User.builder()
                .id(keycloakUserId)
                .firstname(request.firstName())
                .lastname(request.lastName())
                .phoneNumber(request.phoneNumber())
                .email(request.email())
                .isActive(true)
                .isVerified(false)
                .createdAt(LocalDateTime.now())
                .build();
        return userRepository.save(user);
    }

    private MemberProfile saveMemberProfile(String keycloakUserId, RetreatRegistrationRequest request) {
        MemberProfile profile = MemberProfile.builder()
                .keycloakUserId(keycloakUserId)
                .age(request.age())
                .phone(request.phoneNumber())
                .gender(request.gender())
                .build();
        return memberProfileRepository.save(profile);
    }

    private void saveRetreatRegistration(MemberProfile profile, RetreatRegistrationRequest request) {
        RetreatRegistration registration = RetreatRegistration.builder()
                .keycloakUserId(profile.getKeycloakUserId())
                .member(profile)
                .goal(request.goal())
                .preferredMode(request.preferredMode())
                .additionalDetails(request.additionalDetails())
                .gender(request.gender())
                .age(request.age())
                .build();
        retreatRegistrationRepository.save(registration);
    }

    private void rollbackOnFailure(String username, String keycloakUserId, Exception ex) {
        log.error("❌ Retreat registration failed for user: {}", username, ex);
        if (keycloakUserId != null) {
            try {
                keycloakAdminService.deleteUserById(keycloakUserId);
                log.info("Rolled back Keycloak user: {}", keycloakUserId);
            } catch (Exception e) {
                log.error("Keycloak rollback failed", e);
            }
        }
        throw new RegistrationException("Retreat registration failed: " + ex.getMessage(), ex);
    }


    public PaginatedResponse<RetreatRegistrationResponse> getRegistrations(
            Pageable pageable,
            Goal goal,
            Gender gender,
            PreferredMode preferredMode,
            String memberName,
            String keycloakUserId
    ) {
        Specification<RetreatRegistration> spec = Specification.allOf();

        if (goal != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("goal"), goal));
        }
        if (gender != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("gender"), gender));
        }
        if (preferredMode != null) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("preferredMode"), preferredMode));
        }
        if (keycloakUserId != null && !keycloakUserId.isBlank()) {
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("keycloakUserId"), keycloakUserId));
        }
        if (memberName != null && !memberName.isBlank()) {
            String like = "%" + memberName.toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, cq, cb) -> cb.like(
                    cb.lower(root.join("member").join("user").get("firstname")), like
            ));}

        Page<RetreatRegistration> page = retreatRegistrationRepository.findAll(spec, pageable);

        Page<RetreatRegistrationResponse> responsePage = page.map(this::toResponse);
        return PaginatedResponse.fromPage(responsePage);
    }

    private RetreatRegistrationResponse toResponse(RetreatRegistration r) {
        String memberName = null;
        String memberEmail = null;

        if (r.getMember() != null && r.getMember().getUser() != null) {
            memberName = r.getMember().getUser().getFirstname() + " " + r.getMember().getUser().getLastname();
            memberEmail = r.getMember().getUser().getEmail();
        }

        return RetreatRegistrationResponse.builder()
                .id(r.getId())
                .keycloakUserId(r.getKeycloakUserId())
                .goal(r.getGoal())
                .preferredMode(r.getPreferredMode())
                .additionalDetails(r.getAdditionalDetails())
                .age(r.getAge())
                .gender(r.getGender())
                .memberName(memberName)
                .memberEmail(memberEmail)
                .build();
    }

}

