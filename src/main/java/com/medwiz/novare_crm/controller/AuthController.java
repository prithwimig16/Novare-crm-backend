package com.medwiz.novare_crm.controller;

import com.medwiz.novare_crm.dto.request.LoginRequest;
import com.medwiz.novare_crm.dto.request.RefreshTokenRequest;
import com.medwiz.novare_crm.dto.request.UserRequest;
import com.medwiz.novare_crm.dto.response.ApiResponse;
import com.medwiz.novare_crm.dto.response.LoginResponse;
import com.medwiz.novare_crm.dto.response.RefreshTokenResponse;
import com.medwiz.novare_crm.keycloak.KeycloakAdminService;
import com.medwiz.novare_crm.service.AuthService;
import com.medwiz.novare_crm.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final SessionService sessionService;
    private final KeycloakAdminService keycloakAdminService;


    @GetMapping("/prithwi")
    public ResponseEntity<ApiResponse<String>> prithwiTest() {
        return ResponseEntity.ok(ApiResponse.ok("Novare is working now!", null));
    }
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", loginResponse));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(
            @RequestBody @Valid RefreshTokenRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Session-Id") String sessionId
    ) {
        if (request.getRefresh_token() == null || request.getRefresh_token().isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Refresh token is missing"));
        }

        if (!sessionService.isValid(userId, sessionId)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid session"));
        }

        try {
            RefreshTokenResponse newToken = authService.refreshToken(request);
            newToken.setSessionValid(true);

            return ResponseEntity.ok(ApiResponse.ok("New access token fetched successfully", newToken));
        } catch (Exception e) {
            log.error("❌ Failed to refresh token", e);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token refresh failed"));
        }
    }

    @DeleteMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Session-Id") String sessionId
    ) {
        authService.logout(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.ok("Logout successful", "Success"));
    }

    @PostMapping("/register/user")
    public ResponseEntity<ApiResponse<String>> registerUser(@RequestBody @Valid UserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.registerUser(request), "User registered"));
    }


    @DeleteMapping("/keycloak/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteKeyCloakUser(@PathVariable String userId) {
        keycloakAdminService.deleteUserById(userId);
        return ResponseEntity.ok("User deleted: " + userId);
    }

    @DeleteMapping("/keycloak")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteAllKeyCloakUsers() {
        int deletedCount = keycloakAdminService.deleteAllUsers();
        return ResponseEntity.ok("Deleted " + deletedCount + " users from Keycloak");
    }
}

