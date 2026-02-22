package com.hisabkitab.dto;

import com.hisabkitab.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for the Admin Dashboard — system-wide overview.
 *
 * Admins can see data across ALL lenders:
 *   - Total number of users, lenders, borrowers
 *   - System-wide financial stats
 *   - Recent user registrations
 *   - Top lenders by loan volume
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponse {

    // User stats
    private long totalUsers;
    private long totalLenders;
    private long totalBorrowers;
    private long activeUsers;
    private long inactiveUsers;

    // System-wide financial stats
    private long totalLoans;
    private long activeLoans;
    private long completedLoans;
    private BigDecimal totalPrincipalLent;
    private BigDecimal totalInterestGenerated;
    private BigDecimal totalAmountCollected;
    private BigDecimal totalOutstanding;

    // Recent users
    private List<UserSummary> recentUsers;

    // Top lenders
    private List<LenderSummary> topLenders;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserSummary {
        private Long id;
        private String name;
        private String email;
        private Role role;
        private boolean active;
        private LocalDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LenderSummary {
        private Long id;
        private String name;
        private String email;
        private long loanCount;
        private BigDecimal totalLent;
    }
}
