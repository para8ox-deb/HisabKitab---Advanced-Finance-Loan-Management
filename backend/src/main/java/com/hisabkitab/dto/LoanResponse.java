package com.hisabkitab.dto;

import com.hisabkitab.enums.InterestType;
import com.hisabkitab.enums.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for returning loan details to the client.
 * Includes calculated financial fields and borrower name.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanResponse {
    private Long id;
    private Long borrowerId;
    private String borrowerName;

    // Input fields
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer durationMonths;
    private InterestType interestType;
    private LocalDate startDate;
    private LocalDate endDate;

    // Calculated fields
    private BigDecimal totalInterest;
    private BigDecimal totalAmount;
    private BigDecimal monthlyEmi;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;

    private LoanStatus status;
    private LocalDateTime createdAt;
    
    private java.util.List<LoanScheduleResponse> schedules;
}
