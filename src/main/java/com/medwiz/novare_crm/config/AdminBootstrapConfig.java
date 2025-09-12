package com.medwiz.novare_crm.config;

import com.medwiz.novare_crm.entity.User;
import com.medwiz.novare_crm.enums.Role;
import com.medwiz.novare_crm.keycloak.KeycloakAdminService;
import com.medwiz.novare_crm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AdminBootstrapConfig {

    private final KeycloakAdminService keycloakAdminService;
    private final UserRepository userRepository;

    private static final String ADMIN_PHONE = "9436920412";
    private static final String ADMIN_EMAIL = "admin@system.com";
    private static final String ADMIN_PASSWORD = "Admin@123";

    @Bean
    public ApplicationRunner createDefaultAdmin() {
        return args -> {
            try {
                // 1. Check if admin already exists in DB
                if (userRepository.findByPhoneNumber(ADMIN_PHONE).isPresent()) {
                    log.info("✅ Default admin already exists: {}", ADMIN_PHONE);
                    return;
                }

                // 2. Create admin in Keycloak
                keycloakAdminService.createUser(
                        "System", "Admin",
                        ADMIN_PHONE,
                        ADMIN_EMAIL,
                        ADMIN_PASSWORD
                );
                String keycloakUserId = keycloakAdminService.getUserIdByUsername(ADMIN_PHONE);

                // 3. Assign ADMIN role
                keycloakAdminService.assignRole(keycloakUserId, Role.ADMIN);

                // 4. Save into local DB
                User admin = User.builder()
                        .id(keycloakUserId)
                        .firstname("System")
                        .lastname("Admin")
                        .role(Role.ADMIN)
                        .phoneNumber(ADMIN_PHONE)
                        .email(ADMIN_EMAIL)
                        .isActive(true)
                        .isVerified(true)
                        .createdAt(LocalDateTime.now())
                        .build();

                userRepository.save(admin);

                log.info("🚀 Default admin created: {}", ADMIN_EMAIL);

            } catch (Exception e) {
                log.error("❌ Failed to create default admin", e);
            }
        };
    }
}
