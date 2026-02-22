package com.hisabkitab.controller;

import com.hisabkitab.dto.ApiResponse;
import com.hisabkitab.dto.BorrowerRequest;
import com.hisabkitab.dto.BorrowerResponse;
import com.hisabkitab.enums.BorrowerStatus;
import com.hisabkitab.service.BorrowerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * BorrowerController - REST API for managing borrowers.
 *
 * All endpoints require authentication (JWT token in Authorization header).
 * The lender is identified from the JWT token — no need to pass lender_id.
 *
 * Endpoints:
 *   POST   /api/borrowers           → Create a new borrower
 *   GET    /api/borrowers           → Get all borrowers
 *   GET    /api/borrowers/{id}      → Get one borrower by ID
 *   GET    /api/borrowers/status/{s} → Get borrowers by status
 *   PUT    /api/borrowers/{id}      → Update a borrower
 *   PATCH  /api/borrowers/{id}/toggle → Toggle active/inactive
 *   DELETE /api/borrowers/{id}      → Delete a borrower
 *   GET    /api/borrowers/count     → Get borrower count
 */
@RestController
@RequestMapping("/api/borrowers")
@RequiredArgsConstructor
public class BorrowerController {

    private final BorrowerService borrowerService;

    /**
     * POST /api/borrowers
     *
     * Create a new borrower for the logged-in lender.
     *
     * Request Body:
     * {
     *   "name": "Ramesh Kumar",
     *   "phone": "9876543210",
     *   "address": "Delhi",
     *   "notes": "Regular borrower"
     * }
     */
    @PostMapping
    public ResponseEntity<BorrowerResponse> createBorrower(
            @Valid @RequestBody BorrowerRequest request) {
        BorrowerResponse response = borrowerService.createBorrower(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/borrowers
     *
     * Get all borrowers of the logged-in lender.
     * Returns an empty list if no borrowers exist.
     */
    @GetMapping
    public ResponseEntity<List<BorrowerResponse>> getAllBorrowers() {
        List<BorrowerResponse> borrowers = borrowerService.getAllBorrowers();
        return ResponseEntity.ok(borrowers);
    }

    /**
     * GET /api/borrowers/{id}
     *
     * Get a specific borrower by ID.
     * Returns 400 if the borrower doesn't exist or belongs to another lender.
     *
     * @PathVariable extracts the {id} from the URL.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BorrowerResponse> getBorrowerById(@PathVariable Long id) {
        BorrowerResponse response = borrowerService.getBorrowerById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/borrowers/status/ACTIVE
     * GET /api/borrowers/status/INACTIVE
     *
     * Get borrowers filtered by status.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<BorrowerResponse>> getBorrowersByStatus(
            @PathVariable BorrowerStatus status) {
        List<BorrowerResponse> borrowers = borrowerService.getBorrowersByStatus(status);
        return ResponseEntity.ok(borrowers);
    }

    /**
     * PUT /api/borrowers/{id}
     *
     * Update a borrower's details.
     *
     * Request Body:
     * {
     *   "name": "Ramesh Kumar (Updated)",
     *   "phone": "9876543210",
     *   "address": "New Delhi",
     *   "notes": "Updated notes"
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<BorrowerResponse> updateBorrower(
            @PathVariable Long id,
            @Valid @RequestBody BorrowerRequest request) {
        BorrowerResponse response = borrowerService.updateBorrower(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/borrowers/{id}/toggle
     *
     * Toggle a borrower's status between ACTIVE and INACTIVE.
     * Uses PATCH because we're only changing one field.
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<BorrowerResponse> toggleBorrowerStatus(@PathVariable Long id) {
        BorrowerResponse response = borrowerService.toggleBorrowerStatus(id);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/borrowers/{id}
     *
     * Permanently delete a borrower.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteBorrower(@PathVariable Long id) {
        borrowerService.deleteBorrower(id);
        return ResponseEntity.ok(new ApiResponse(true, "Borrower deleted successfully!"));
    }

    /**
     * GET /api/borrowers/count
     *
     * Get the total number of borrowers for the logged-in lender.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getBorrowerCount() {
        long count = borrowerService.getBorrowerCount();
        return ResponseEntity.ok(count);
    }

    /**
     * POST /api/borrowers/1/link
     *
     * Link a borrower to a User account with BORROWER role.
     * This enables the borrower to log in to the Borrower Portal.
     *
     * Request Body: { "email": "ramesh@test.com" }
     */
    @PostMapping("/{id}/link")
    public ResponseEntity<BorrowerResponse> linkBorrowerToUser(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required!");
        }
        return ResponseEntity.ok(borrowerService.linkBorrowerToUser(id, email));
    }
}
