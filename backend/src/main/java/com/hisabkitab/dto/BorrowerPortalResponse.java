package com.hisabkitab.dto;

import com.hisabkitab.enums.InterestType;
import com.hisabkitab.enums.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for the Borrower Portal — shows the borrower a summary
 * of all their loans in one view.
 *
 * This is tailored for the BORROWER's perspective:
 *   - They see their loans from ALL lenders
 *   - They see lender name (instead of borrower name)
 *   - They see payment history
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowerPortalResponse {

    // Borrower's info
    private String borrowerName;
    private String phone;
    private String address;

    // Summary stats
    private int totalLoans;
    private int activeLoans;
    private int completedLoans;
    private BigDecimal totalBorrowed;
    private BigDecimal totalPaid;
    private BigDecimal totalOutstanding;

    // Loan details
    private List<BorrowerLoanView> loans;

    /**
     * A loan as seen from the BORROWER's perspective.
     * Shows lender name instead of borrower name.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BorrowerLoanView {
        private Long loanId;
        private String lenderName;
        private BigDecimal principalAmount;
        private BigDecimal interestRate;
        private InterestType interestType;
        private Integer durationMonths;
        private BigDecimal monthlyEmi;
        private BigDecimal totalAmount;
        private BigDecimal paidAmount;
        private BigDecimal remainingAmount;
        private LoanStatus status;
        private LocalDate startDate;
        private LocalDate endDate;
    }
}
