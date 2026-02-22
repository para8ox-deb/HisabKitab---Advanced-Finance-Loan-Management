package com.hisabkitab.controller;

import com.hisabkitab.dto.DashboardResponse;
import com.hisabkitab.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * DashboardController - Single endpoint for the lender's dashboard.
 *
 * GET /api/dashboard → Returns everything:
 *   - Borrower counts
 *   - Loan counts
 *   - Financial summary
 *   - Upcoming EMIs
 *   - Recent loans
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * GET /api/dashboard
     *
     * One call = full dashboard data.
     * The frontend makes ONE request and gets all dashboard widgets populated.
     */
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }
}
