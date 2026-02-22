package com.hisabkitab.controller;

import com.hisabkitab.dto.AdminDashboardResponse;
import com.hisabkitab.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AdminController — REST API for system administrators.
 *
 * These endpoints are accessible ONLY by users with ADMIN role.
 * SecurityConfig rule: .requestMatchers("/api/admin/**").hasRole("ADMIN")
 *
 * Endpoints:
 *   GET   /api/admin/dashboard       → System-wide dashboard
 *   GET   /api/admin/users           → List all users
 *   PATCH /api/admin/users/{id}/toggle → Enable/disable a user
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * GET /api/admin/dashboard
     *
     * Returns system-wide statistics:
     * user counts, loan stats, financial totals,
     * recent registrations, top lenders.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getAdminDashboard() {
        return ResponseEntity.ok(adminService.getAdminDashboard());
    }

    /**
     * GET /api/admin/users
     *
     * List all registered users in the system.
     * Shows ID, name, email, role, active status.
     */
    @GetMapping("/users")
    public ResponseEntity<List<AdminDashboardResponse.UserSummary>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    /**
     * PATCH /api/admin/users/1/toggle
     *
     * Toggle a user's active status.
     * Active → Inactive: User can no longer log in.
     * Inactive → Active: User can log in again.
     */
    @PatchMapping("/users/{userId}/toggle")
    public ResponseEntity<AdminDashboardResponse.UserSummary> toggleUserStatus(
            @PathVariable Long userId) {
        return ResponseEntity.ok(adminService.toggleUserStatus(userId));
    }

    /**
     * GET /api/admin/users/{userId}
     *
     * Retrieve detailed info for a specific user, including loan stats and a list of borrowers.
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<com.hisabkitab.dto.UserDetailResponse> getUserDetails(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.getUserDetails(userId));
    }

    /**
     * DELETE /api/admin/users/{userId}
     *
     * Hard delete a user exactly with all nested entities (loans, borrowers, EMIs).
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /api/admin/borrowers/{borrowerId}
     *
     * Hard delete a borrower exactly with all nested entities (loans, EMIs).
     */
    @DeleteMapping("/borrowers/{borrowerId}")
    public ResponseEntity<Void> deleteBorrower(@PathVariable Long borrowerId) {
        adminService.deleteBorrower(borrowerId);
        return ResponseEntity.ok().build();
    }
}
