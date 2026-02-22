package com.hisabkitab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Unified Dashboard DTO — one API call returns everything the frontend
 * needs to render the lender's dashboard.
 *
 * Sections:
 *   1. Overview counts (total borrowers, loans, etc.)
 *   2. Financial summary (total lent, earned, outstanding)
 *   3. Recent loans (last 5 created)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    // ===== Overview Counts =====
    private long totalBorrowers;
    private long activeBorrowers;
    private long totalLoans;
    private long activeLoans;
    private long completedLoans;

    // ===== Financial Summary =====
    private BigDecimal totalAmountLent;
    private BigDecimal totalInterestEarned;
    private BigDecimal totalOutstanding;
    private BigDecimal totalCollected;

    // ===== Recent Loans (last 5 created) =====
    private List<RecentLoan> recentLoans;

    /**
     * Represents a recent loan for the dashboard.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentLoan {
        private Long loanId;
        private String borrowerName;
        private BigDecimal principalAmount;
        private BigDecimal interestRate;
        private String status;
        private java.time.LocalDateTime createdAt;
    }
}
