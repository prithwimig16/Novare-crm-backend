package com.medwiz.novare_crm.controller;

import com.medwiz.novare_crm.dto.request.RetreatRegistrationRequest;
import com.medwiz.novare_crm.dto.response.ApiResponse;
import com.medwiz.novare_crm.dto.response.PaginatedResponse;
import com.medwiz.novare_crm.dto.response.RetreatRegistrationResponse;
import com.medwiz.novare_crm.enums.Gender;
import com.medwiz.novare_crm.enums.Goal;
import com.medwiz.novare_crm.enums.PreferredMode;
import com.medwiz.novare_crm.service.RetreatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/retreats")
@RequiredArgsConstructor
public class RetreatController {

    private final RetreatService retreatService;

    /**
     * First-time member registers with retreat
     */

    @PostMapping("/register-first")
    public ResponseEntity<ApiResponse<String>> registerMemberForFirstRetreat(
            @RequestBody @Valid RetreatRegistrationRequest request
    ) {
        String result = retreatService.registerMemberForFirstRetreat(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "First-time member and retreat registration successful"));
    }

    /**
     * Existing member books another retreat
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> registerForRetreat(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody @Valid RetreatRegistrationRequest request
    ) {
        String result = retreatService.registerForRetreat(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "Retreat registration successful"));
    }

    @GetMapping
   // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaginatedResponse<RetreatRegistrationResponse>>> list(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) Goal goal,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) PreferredMode preferredMode,
            @RequestParam(required = false) String memberName,
            @RequestParam(required = false) String keycloakUserId
    ) {
        var data = retreatService.getRegistrations(pageable, goal, gender, preferredMode, memberName, keycloakUserId);
        return ResponseEntity.ok(ApiResponse.ok( "Fetched retreat registrations",data));
    }
}

