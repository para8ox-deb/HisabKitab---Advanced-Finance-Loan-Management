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
 * DTO for the detailed view of a single User (Lender) in the Admin Dashboard.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;
    
    // Aggregated statistics for this user's loans
    private long totalLoans;
    private long activeLoans;
    private BigDecimal totalPrincipalLent;
    private BigDecimal totalInterestGenerated;
    private BigDecimal totalAmountCollected;

    // List of Borrowers managed by this User
    private List<BorrowerSummary> borrowers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BorrowerSummary {
        private Long id;
        private String name;
        private String phone;
        private int activeLoanCount;
        private BigDecimal totalOwed;
    }
}
