package com.medwiz.novare_crm.service;

import com.medwiz.novare_crm.dto.request.DoctorUserRequest;
import com.medwiz.novare_crm.dto.request.LoginRequest;
import com.medwiz.novare_crm.dto.request.RefreshTokenRequest;
import com.medwiz.novare_crm.dto.request.UserRequest;
import com.medwiz.novare_crm.dto.response.LoginResponse;
import com.medwiz.novare_crm.dto.response.RefreshTokenResponse;
import com.medwiz.novare_crm.entity.User;
import com.medwiz.novare_crm.enums.Role;
import com.medwiz.novare_crm.exception.LoginException;
import com.medwiz.novare_crm.exception.RegistrationException;
import com.medwiz.novare_crm.keycloak.KeycloakAdminService;
import com.medwiz.novare_crm.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final SessionService sessionService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final NovareRegistrationService novareRegistrationService;

    @Value("${app.base-url}")
    private String baseUrl;

    public LoginResponse login(LoginRequest request) {
        Map<String, Object> tokenResponse;

        try {
            tokenResponse = keycloakAdminService.getUserToken(
                    request.getPhoneNumber(),
                    request.getPassword()
            );
        } catch (Exception ex) {
            log.error("❌ Keycloak Error", ex);
            if (ex.getMessage() != null && ex.getMessage().contains("401")) {
                throw new LoginException("Wrong username or password!", ex);
            }
            throw new LoginException("Login failed! Authentication server is down!", ex);
        }

        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new LoginException("User metadata not found"));

        // ✅ Ensure only one active session per user
        String sessionId = sessionService.createOrUpdateSession(user.getId());

        return LoginResponse.builder()
                .userId(user.getId())
                .firstName(user.getFirstname())
                .lastName(user.getLastname())
                .sessionId(sessionId)
                .isVerified(user.isVerified())
                .isActive(user.isActive())
                .accessToken((String) tokenResponse.get("access_token"))
                .refreshToken((String) tokenResponse.get("refresh_token"))
                .message("Login successful")
                .build();
    }

    @Transactional
    public void logout(String userId, String sessionId) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new EntityNotFoundException("User not found with ID: " + userId);
        }


        boolean valid = sessionService.isValid(userId, sessionId);
        if (!valid) {
            throw new LoginException("Invalid session. Cannot logout.");
        }
        sessionService.invalidateSession(userId,sessionId);
    }

    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        Map<String, Object> newTokens = keycloakAdminService.refreshUserToken(request.getRefresh_token());
        String newAccessToken = (String) newTokens.get("access_token");
        String newRefreshToken = (String) newTokens.get("refresh_token");
        return RefreshTokenResponse.builder()
                .access_token(newAccessToken)
                .refresh_token(newRefreshToken)
                .sessionValid(true).build();


    }


    @Transactional(rollbackFor = {RegistrationException.class, DataIntegrityViolationException.class})
    public String registerUser(UserRequest request) {
        String username = request.getPhoneNumber();
        log.info("📌 Registering user in Keycloak: {}", username);

        if (userRepository.findByPhoneNumber(username).isPresent()) {
            throw new RegistrationException("User already exists with this phone number.");
        }

        String keycloakUserId = null;

        try {
            // Step 1: Create in Keycloak
            keycloakAdminService.createUser(
                    request.getFirstName(),
                    request.getLastName(),
                    username,
                    request.getEmail(),
                    request.getPassword()
            );
            log.info("✅ Keycloak user created: {}", username);

            keycloakUserId = keycloakAdminService.getUserIdByUsername(username);
            keycloakAdminService.assignRole(keycloakUserId, mapUserRoleToKeycloakRole(request.getRole()));

            // Step 2: Save to DB (use String, not UUID)
            return switch (request.getRole()) {
                case DOCTOR -> novareRegistrationService.registerDoctor((DoctorUserRequest) request, keycloakUserId);
                case MEMBER -> novareRegistrationService.registerMember(request, keycloakUserId);
                case ADMIN -> novareRegistrationService.registerAdmin(request, keycloakUserId);
                default -> throw new RegistrationException("Unsupported role: " + request.getRole());
            };

        } catch (Exception ex) {
            log.error("❌ Registration failed for user: {}", username, ex);
            if (keycloakUserId != null) {
                try {
                    rollbackKeycloakUser(keycloakUserId);
                } catch (Exception rollbackEx) {
                    log.error("⚠️ Rollback failed for Keycloak user: {}", keycloakUserId, rollbackEx);
                }
            }

            if (ex instanceof ConstraintViolationException cve) {
                throw new RegistrationException("Validation failed: " + cve.getMessage(), cve);
            } else if (ex instanceof HttpClientErrorException.Conflict conflictEx) {
                throw new RegistrationException("User already exists with this email.", conflictEx);
            } else if (ex instanceof HttpClientErrorException httpEx) {
                throw new RegistrationException("Keycloak error: " + httpEx.getResponseBodyAsString(), httpEx);
            } else if (ex instanceof RegistrationException re) {
                throw re;
            } else {
                throw new RegistrationException("Registration failed due to unexpected error.", ex);
            }
        }
    }

    private void rollbackKeycloakUser(String keycloakUserId) {
        if (keycloakUserId != null) {
            try {
                keycloakAdminService.deleteUserById(keycloakUserId);
                log.info("Rolled back Keycloak user: {}", keycloakUserId);
            } catch (Exception e) {
                log.error("Keycloak rollback failed", e);
            }
        }
    }


    private String mapUserRoleToKeycloakRole(Role role) {
        return switch (role) {
            case ADMIN -> "ADMIN";
            case DOCTOR -> "DOCTOR";
            case MEMBER -> "MEMBER";
        };
    }


}
