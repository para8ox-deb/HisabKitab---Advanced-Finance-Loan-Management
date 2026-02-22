package com.hisabkitab.controller;

import com.hisabkitab.dto.LoanRequest;
import com.hisabkitab.dto.LoanResponse;
import com.hisabkitab.enums.LoanStatus;
import com.hisabkitab.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * LoanController - REST API for managing loans and EMI payments.
 *
 * All endpoints require authentication (JWT token in Authorization header).
 *
 * Endpoints:
 *   POST   /api/loans                    → Create a new loan (auto-generates EMI schedule)
 *   GET    /api/loans                    → Get all loans
 *   GET    /api/loans/{id}               → Get loan by ID
 *   GET    /api/loans/status/{status}    → Get loans by status
 *   GET    /api/loans/borrower/{id}      → Get loans for a borrower
 *   GET    /api/loans/summary            → Get dashboard summary stats
 */
@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    /**
     * POST /api/loans
     *
     * Creates a new loan and auto-generates the EMI repayment schedule.
     *
     * Request Body:
     * {
     *   "borrowerId": 1,
     *   "principalAmount": 50000,
     *   "interestRate": 12,
     *   "durationMonths": 6,
     *   "startDate": "2026-03-01"
     * }
     *
     * The server calculates:
     * - Total Interest (Simple Interest formula)
     * - Total Amount (Principal + Interest)
     * - Monthly EMI (Total / Months)
     * - Full EMI schedule with due dates
     */
    @PostMapping
    public ResponseEntity<LoanResponse> createLoan(@Valid @RequestBody LoanRequest request) {
        LoanResponse response = loanService.createLoan(request);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/loans/{id}
     *
     * Edits an existing loan and recalculates the EMI schedule backwards.
     */
    @PutMapping("/{id}")
    public ResponseEntity<LoanResponse> updateLoan(@PathVariable Long id, @Valid @RequestBody LoanRequest request) {
        LoanResponse response = loanService.updateLoan(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/loans
     */
    @GetMapping
    public ResponseEntity<List<LoanResponse>> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }

    /**
     * GET /api/loans/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<LoanResponse> getLoanById(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    }

    /**
     * GET /api/loans/status/ACTIVE
     * GET /api/loans/status/COMPLETED
     * GET /api/loans/status/DEFAULTED
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<LoanResponse>> getLoansByStatus(@PathVariable LoanStatus status) {
        return ResponseEntity.ok(loanService.getLoansByStatus(status));
    }

    /**
     * GET /api/loans/borrower/1
     *
     * Get all loans for a specific borrower.
     */
    @GetMapping("/borrower/{borrowerId}")
    public ResponseEntity<List<LoanResponse>> getLoansByBorrower(@PathVariable Long borrowerId) {
        return ResponseEntity.ok(loanService.getLoansByBorrower(borrowerId));
    }



    /**
     * GET /api/loans/summary
     *
     * Dashboard summary stats:
     * - Total loans, active loans, completed loans
     * - Total amount lent, interest earned, outstanding balance
     */
    @GetMapping("/summary")
    public ResponseEntity<LoanService.LoanSummary> getLoanSummary() {
        return ResponseEntity.ok(loanService.getLoanSummary());
    }

    /**
     * PATCH /api/loans/schedule/{scheduleId}/pay
     *
     * Mark a specific monthly interest schedule as PAID with an optional note.
     */
    @PatchMapping("/schedule/{scheduleId}/pay")
    public ResponseEntity<com.hisabkitab.dto.LoanScheduleResponse> paySchedule(
            @PathVariable Long scheduleId,
            @jakarta.validation.Valid @RequestBody com.hisabkitab.dto.SchedulePaymentRequest request) {
        return ResponseEntity.ok(loanService.paySchedule(scheduleId, request));
    }

    /**
     * POST /api/loans/{id}/prepay
     *
     * Make a partial prepayment against the principal.
     */
    @PostMapping("/{id}/prepay")
    public ResponseEntity<com.hisabkitab.dto.PrepaymentResponse> prepayLoan(
            @PathVariable Long id, 
            @Valid @RequestBody com.hisabkitab.dto.PrepaymentRequest request) {
        return ResponseEntity.ok(loanService.prepayLoan(id, request));
    }

    /**
     * POST /api/loans/{id}/settle
     *
     * Settle the loan completely (pay off remaining balance and mark as COMPLETED).
     */
    @PostMapping("/{id}/settle")
    public ResponseEntity<com.hisabkitab.dto.ApiResponse> settleLoan(@PathVariable Long id) {
        loanService.settleLoan(id);
        return ResponseEntity.ok(new com.hisabkitab.dto.ApiResponse(true, "Loan successfully settled"));
    }

    /**
     * DELETE /api/loans/{id}
     *
     * Deletes a loan.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<com.hisabkitab.dto.ApiResponse> deleteLoan(@PathVariable Long id) {
        loanService.deleteLoan(id);
        return ResponseEntity.ok(new com.hisabkitab.dto.ApiResponse(true, "Loan successfully deleted"));
    }
}
