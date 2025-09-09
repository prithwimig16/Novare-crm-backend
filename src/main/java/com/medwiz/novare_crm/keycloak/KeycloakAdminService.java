package com.medwiz.novare_crm.keycloak;

import com.medwiz.novare_crm.enums.Role;
import com.medwiz.novare_crm.exception.RegistrationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakAdminService {
    private static final String LOG_PREFIX = "[🔐 KEYCLOAK] ";
    private final KeycloakProperties keycloakProps;
    private final RestTemplate restTemplate;
    private String cachedAdminToken;
    private long tokenExpiryTime;

    /**
     * Get admin token via client_credentials flow
     */

    private String getAdminToken() {

        log.info(LOG_PREFIX + "🔐 Fetching new admin token from Keycloak...");

        if (cachedAdminToken != null && System.currentTimeMillis() < tokenExpiryTime) {
            return cachedAdminToken; // ✅ Use cached token if still valid
        }

        log.info(LOG_PREFIX + "🔧 Token URL: {}/realms/{}/protocol/openid-connect/token", keycloakProps.getBaseUrl(), keycloakProps.getRealm());
        log.info(LOG_PREFIX + "🧾 Using client_id: {}, client_secret: {}", keycloakProps.getClientId(), keycloakProps.getClientSecret().substring(0, 4) + "****"); // mask secret


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "client_id=" + keycloakProps.getClientId() +
                "&client_secret=" + keycloakProps.getClientSecret() +
                "&grant_type=client_credentials";

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {


            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    keycloakProps.getBaseUrl() + "/realms/" + keycloakProps.getRealm() + "/protocol/openid-connect/token",
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<>() {
                    }
            );

            log.info(LOG_PREFIX + "📥 Token response status: {}", response.getStatusCode());


            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException(LOG_PREFIX + "Failed to fetch token: " + response.getStatusCode());
            }


            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !responseBody.containsKey("access_token")) {
                throw new RuntimeException("Token response is missing access_token");
            }
            log.info(LOG_PREFIX + "✅ Access token received. Expires in: {} seconds", responseBody.get("expires_in"));
            cachedAdminToken = (String) responseBody.get("access_token");

            Integer expiresIn = (Integer) responseBody.get("expires_in");
            tokenExpiryTime = System.currentTimeMillis() + (expiresIn - 60) * 1000L; // Subtract buffer to avoid expiration

            return cachedAdminToken;

        } catch (HttpClientErrorException ex) {
            log.error(LOG_PREFIX + "❌ Response body: {}", ex.getResponseBodyAsString());
            throw new RuntimeException("Keycloak token fetch failed: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString());
        } catch (Exception ex) {

            log.error(LOG_PREFIX + "❌ Failed to fetch admin token: {}", ex.getMessage());
            throw new RuntimeException("Unexpected error fetching admin token", ex);
        }
    }

    /**
     * Create a user in Keycloak
     */
    public void createUser(String firstName, String lastName, String username, String email, String password) {

        log.info(LOG_PREFIX + "📌Keycloak creating... user: {}", username);
        String token = getAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> userPayload = new HashMap<>();
        userPayload.put("username", username);
        userPayload.put("firstName", firstName);
        userPayload.put("lastName", lastName);
        userPayload.put("email", email);
        userPayload.put("emailVerified", true);
        userPayload.put("enabled", true);
        userPayload.put("requiredActions", List.of());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(userPayload, headers);

        log.info(LOG_PREFIX + "📤 Sending request to Keycloak to create user at 1: {}/admin/realms/{}/users", keycloakProps.getBaseUrl(), keycloakProps.getRealm());
        log.info(LOG_PREFIX + "📦 Payload 1: {}", userPayload);

        try {
            restTemplate.postForEntity(
                    keycloakProps.getBaseUrl() + "/admin/realms/" + keycloakProps.getRealm() + "/users",
                    request,
                    String.class
            );

            log.info(LOG_PREFIX + "📤 Sending request to Keycloak to create user at 2: {}/admin/realms/{}/users", keycloakProps.getBaseUrl(), keycloakProps.getRealm());
            log.info(LOG_PREFIX + "📦 Payload 2: {}", userPayload);

            log.info(LOG_PREFIX + "✅ Created Keycloak user: {}", username);

            // Get userId
            String userId = getUserIdByUsername(username);

            // 1. Delete any auto-created temp credentials
            deleteUserCredentials(userId);
            // 2. Set a permanent password
            setPermanentPassword(userId, password);
            UUID.fromString(userId);
        } catch (HttpClientErrorException ex) {
            log.error(LOG_PREFIX + "❌ Failed to create Keycloak user: {}", ex.getMessage(), ex);
            if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                log.error(LOG_PREFIX + "❌ Failed to create Keycloak user: user already exist", ex);
                throw new RegistrationException("User already exist in Keycloak");
            } else {
                log.error(LOG_PREFIX + "❌ Failed to create user in Keycloak", ex);
                throw new RegistrationException("Failed to create user in Keycloak", ex);
            }

        }


    }

    public void setPermanentPassword(String userId, String password) {
        String token = getAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "password");
        payload.put("value", password);
        payload.put("temporary", false); // ✅ Critical

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            restTemplate.put(
                    keycloakProps.getBaseUrl() + "/admin/realms/" + keycloakProps.getRealm()
                            + "/users/" + userId + "/reset-password",
                    request
            );
            log.info(LOG_PREFIX + "🔐 Password set for user {}", userId);
        } catch (Exception ex) {
            log.error(LOG_PREFIX + "❌ Failed to set password for user {}: {}", userId, ex.getMessage());
            throw new RegistrationException("Failed to set password", ex);
        }
    }


    public void deleteUserCredentials(String userId) {
        String token = getAdminToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        // GET credentials
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                keycloakProps.getBaseUrl() + "/admin/realms/" + keycloakProps.getRealm() + "/users/" + userId + "/credentials",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );

        List<Map<String, Object>> creds = response.getBody();
        if (creds != null) {
            for (Map<String, Object> cred : creds) {
                String credentialId = (String) cred.get("id");
                restTemplate.exchange(
                        keycloakProps.getBaseUrl() + "/admin/realms/" + keycloakProps.getRealm() + "/users/" + userId + "/credentials/" + credentialId,
                        HttpMethod.DELETE,
                        new HttpEntity<>(headers),
                        Void.class
                );
            }
        }
    }


    /**
     * Get userId of Keycloak user by username
     */
    public String getUserIdByUsername(String username) {
        String token = getAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                keycloakProps.getBaseUrl() + "/admin/realms/" + keycloakProps.getRealm() + "/users?username=" + username,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );

        List<Map<String, Object>> users = response.getBody();
        if (users != null && !users.isEmpty()) {
            String userId = (String) users.get(0).get("id");
            log.info("🔍 Found Keycloak userId for username {}: {}", username, userId);
            return userId;
        }

        throw new RegistrationException("Keycloak user not found after creation: " + username);
    }

    /**
     * Assign a realm role to a Keycloak user
     */
    public void assignRole(String userId, Role roleName) {
        String token = getAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            // Fetch role details
            ResponseEntity<Map<String, Object>> roleResponse = restTemplate.exchange(
                    keycloakProps.getBaseUrl() + "/admin/realms/" + keycloakProps.getRealm() + "/roles/" + roleName,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<>() {
                    }
            );

            Map<String, Object> role = roleResponse.getBody();
            if (role == null || !role.containsKey("id")) {
                throw new RegistrationException("Keycloak role not found: " + roleName);
            }

            // Assign role to user
            String url = keycloakProps.getBaseUrl() + "/admin/realms/" + keycloakProps.getRealm()
                    + "/users/" + userId + "/role-mappings/realm";

            restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(List.of(role), headers),
                    String.class
            );

            log.info("✅ Assigned role '{}' to user {}", roleName, userId);

        } catch (HttpClientErrorException.NotFound ex) {
            log.error("❌ Role not found in Keycloak: {}", roleName);
            throw new RegistrationException("Keycloak role '" + roleName + "' not found", ex);
        } catch (Exception ex) {
            log.error("❌ Failed to assign role '{}' to user {}: {}", roleName, userId, ex.getMessage());
            throw new RegistrationException("Failed to assign role in Keycloak", ex);
        }
    }

    /**
     * Get access & refresh token via Resource Owner Password Flow (for login)
     */
    public Map<String, Object> getUserToken(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", keycloakProps.getClientId());
        form.add("client_secret", keycloakProps.getClientSecret());
        form.add("username", username);
        form.add("password", password);
        form.add("scope", "offline_access");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    keycloakProps.getBaseUrl() + "/realms/" + keycloakProps.getRealm() + "/protocol/openid-connect/token",
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<>() {
                    }
            );

            log.info("🔐 Fetched token for user {}", username);
            return response.getBody();

        } catch (HttpClientErrorException ex) {
            log.error("❌ Failed to fetch token for user {}: {}", username, ex.getMessage());
            throw new RegistrationException("Failed to login via Keycloak", ex);
        }

    }

    public Map<String, Object> refreshUserToken(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", keycloakProps.getClientId());
        form.add("client_secret", keycloakProps.getClientSecret());
        form.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    keycloakProps.getBaseUrl() + "/realms/" + keycloakProps.getRealm() + "/protocol/openid-connect/token",
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<>() {
                    }
            );

            log.info("🔁 Refreshed token using refresh_token");
            return response.getBody();

        } catch (HttpClientErrorException ex) {
            log.error("❌ Failed to refresh token: {}", ex.getMessage());
            throw new RuntimeException("Token refresh failed", ex);
        }
    }


    public void deleteUserById(String userId) {
        String token = getAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        try {
            restTemplate.exchange(
                    keycloakProps.getBaseUrl() + "/admin/realms/" + keycloakProps.getRealm() + "/users/" + userId,
                    HttpMethod.DELETE,
                    new HttpEntity<>(headers),
                    Void.class
            );
            log.info("🗑️ Deleted Keycloak user {}", userId);
        } catch (Exception e) {
            log.error("❌ Failed to delete user {} from Keycloak: {}", userId, e.getMessage());
            throw new RegistrationException("Failed to delete Keycloak user", e);
        }
    }

    public int deleteAllUsers() {
        String token = getAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                keycloakProps.getBaseUrl() + "/admin/realms/" + keycloakProps.getRealm() + "/users",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );

        List<Map<String, Object>> users = response.getBody();
        int deletedCount = 0;

        if (users != null) {
            for (Map<String, Object> user : users) {
                String userId = (String) user.get("id");
                try {
                    deleteUserById(userId);
                    deletedCount++;
                } catch (Exception e) {
                    log.warn("⚠️ Could not delete user {}: {}", userId, e.getMessage());
                }
            }
        }

        return deletedCount;
    }

    public void changePasswordForUser(String userId, String newPassword) {
        log.info("🔄 Changing password for user {}", userId);
        setPermanentPassword(userId, newPassword);
    }

    public void updateUsername(String userId, String newPhoneNumber) {
        String token = getAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();
        payload.put("username", newPhoneNumber);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            restTemplate.exchange(
                    keycloakProps.getBaseUrl() + "/admin/realms/" + keycloakProps.getRealm() + "/users/" + userId,
                    HttpMethod.PUT,
                    request,
                    Void.class
            );
            log.info("✅ Updated username (phone number) for user {}", userId);
        } catch (Exception e) {
            log.error("❌ Failed to update username: {}", e.getMessage());
            throw new RegistrationException("Failed to update phone number (username)", e);
        }
    }


}