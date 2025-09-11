package com.medwiz.novare_crm.controller;

import com.medwiz.novare_crm.dto.request.RetreatRegistrationRequest;
import com.medwiz.novare_crm.dto.response.ApiResponse;
import com.medwiz.novare_crm.service.RetreatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}

