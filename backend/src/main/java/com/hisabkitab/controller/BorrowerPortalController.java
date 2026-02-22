package com.hisabkitab.controller;

import com.hisabkitab.dto.BorrowerPortalResponse;
import com.hisabkitab.service.BorrowerPortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * BorrowerPortalController — Borrower-facing REST API.
 *
 * These endpoints are accessible ONLY by users with BORROWER role.
 * The security rule in SecurityConfig:
 *   .requestMatchers("/api/borrower/**").hasRole("BORROWER")
 *
 * Note: We use "/api/borrower-portal" (NOT "/api/borrower") because
 * the SecurityConfig uses "/api/borrower/**" pattern. We update the
 * security config to also cover /api/borrower-portal/**.
 *
 * Endpoints:
 *   GET /api/borrower-portal/dashboard    → Full borrower dashboard
 */
@RestController
@RequestMapping("/api/borrower-portal")
@RequiredArgsConstructor
public class BorrowerPortalController {

    private final BorrowerPortalService borrowerPortalService;

    /**
     * GET /api/borrower-portal/dashboard
     *
     * Returns the borrower's complete dashboard:
     *   - Personal info
     *   - Loan summary (total borrowed, paid, outstanding)
     *   - All loans with details
     */
    @GetMapping("/dashboard")
    public ResponseEntity<BorrowerPortalResponse> getDashboard() {
        return ResponseEntity.ok(borrowerPortalService.getPortalDashboard());
    }

    /**
     * PUT /api/borrower-portal/profile
     *
     * Update the borrower's personal information (name, phone, address).
     * Optionally updates the password.
     */
    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(@jakarta.validation.Valid @RequestBody com.hisabkitab.dto.BorrowerProfileUpdateRequest request) {
        borrowerPortalService.updateMyProfile(request);
        return ResponseEntity.ok().build();
    }
}
