package com.medwiz.novare_crm.dto.response;

import com.medwiz.novare_crm.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String firstName;
    private String lastName;
    private String userId;
    private String sessionId;
    private Role role;
    private boolean isVerified;
    private boolean isActive;
    private String accessToken;
    private String refreshToken;
    private String message;
}
