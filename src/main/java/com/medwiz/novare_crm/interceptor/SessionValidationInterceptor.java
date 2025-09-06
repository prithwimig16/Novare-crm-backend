package com.medwiz.novare_crm.interceptor;

import com.medwiz.novare_crm.entity.User;
import com.medwiz.novare_crm.repository.UserRepository;
import com.medwiz.novare_crm.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionValidationInterceptor implements HandlerInterceptor {

    private final SessionService sessionService;
    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userIdHeader = request.getHeader("X-User-Id");   // Keycloak "sub"
        String sessionIdHeader = request.getHeader("X-Session-Id");

        if (userIdHeader == null || sessionIdHeader == null) {
            log.warn("🔐 Missing headers: X-User-Id={}, X-Session-Id={}", userIdHeader, sessionIdHeader);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing authentication headers");
            return false;
        }

        // ✅ No UUID parsing, keep Keycloak userId as String
        String userId = userIdHeader;

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("🔐 User not found for userId={}, sessionId={}", userId, sessionIdHeader);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
            return false;
        }

        if (!sessionService.isValid(userId, sessionIdHeader)) {
            log.warn("🔐 Invalid session for userId={}, sessionId={}", userId, sessionIdHeader);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid session");
            return false;
        }

        return true;
    }
}
