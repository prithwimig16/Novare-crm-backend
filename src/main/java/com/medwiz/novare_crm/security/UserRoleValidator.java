package com.medwiz.novare_crm.security;

import com.medwiz.novare_crm.enums.Role;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class UserRoleValidator {

    public boolean isValidRole(String role) {
        return Arrays.stream(Role.values())
                .anyMatch(r -> r.getName().equalsIgnoreCase(role));
    }

    public void validateOrThrow(String role) {
        if (!isValidRole(role)) {
            throw new IllegalArgumentException("❌ Invalid role: " + role);
        }
    }
}

