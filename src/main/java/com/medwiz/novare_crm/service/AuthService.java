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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SessionService sessionService;
    private final UserRepository userRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final NovareRegistrationService novareRegistrationService;

    public LoginResponse login(LoginRequest request) {
        Map<String, Object> tokenResponse;
        try {
            tokenResponse = keycloakAdminService.getUserToken(
                    request.getContact(),
                    request.getPassword()
            );
        } catch (Exception ex) {
            log.error("❌ Keycloak Error", ex);
            if (ex.getMessage() != null && ex.getMessage().contains("401")) {
                throw new LoginException( ex.getMessage());
            }
            throw new LoginException( ex.getMessage());
        }

        User user = userRepository.findByPhoneNumber(request.getContact())
                .orElseThrow(() -> new LoginException("User metadata not found"));

        if(user.getRole()!= Role.ADMIN){
            throw new LoginException("Wrong username or password!");
        }

        String sessionId = sessionService.createOrUpdateSession(user.getId());

        return LoginResponse.builder()
                .userId(user.getId())
                .firstName(user.getFirstname())
                .lastName(user.getLastname())
                .role(user.getRole())
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
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        if (!sessionService.isValid(userId, sessionId)) {
            throw new LoginException("Invalid session. Cannot logout.");
        }
        sessionService.invalidateSession(userId, sessionId);
    }

    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        Map<String, Object> newTokens = keycloakAdminService.refreshUserToken(request.getRefresh_token());
        return RefreshTokenResponse.builder()
                .access_token((String) newTokens.get("access_token"))
                .refresh_token((String) newTokens.get("refresh_token"))
                .sessionValid(true)
                .build();
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
            keycloakAdminService.createUser(
                    request.getFirstName(),
                    request.getLastName(),
                    username,
                    request.getEmail(),
                    request.getPassword()
            );

            keycloakUserId = keycloakAdminService.getUserIdByUsername(username);
            keycloakAdminService.assignRole(keycloakUserId, request.getRole());

            return switch (request.getRole()) {
                case DOCTOR -> novareRegistrationService.registerDoctor((DoctorUserRequest) request, keycloakUserId);
                case MEMBER -> novareRegistrationService.registerMember(request, keycloakUserId);
                case ADMIN -> novareRegistrationService.registerAdmin(request, keycloakUserId);
            };
        } catch (Exception ex) {
            rollbackOnFailure(username, keycloakUserId, ex);
            return null;
        }
    }

    private void rollbackOnFailure(String username, String keycloakUserId, Exception ex) {
        log.error("❌ Registration failed for user: {}", username, ex);
        if (keycloakUserId != null) {
            try {
                keycloakAdminService.deleteUserById(keycloakUserId);
                log.info("Rolled back Keycloak user: {}", keycloakUserId);
            } catch (Exception e) {
                log.error("Keycloak rollback failed", e);
            }
        }
        throw new RegistrationException("Registration failed: " + ex.getMessage(), ex);
    }
}

