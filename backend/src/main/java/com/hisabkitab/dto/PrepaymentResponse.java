package com.hisabkitab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response after making a partial principal prepayment.
 *
 * Shows the BEFORE and AFTER state so the borrower/lender
 * can see exactly what changed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrepaymentResponse {

    private Long loanId;
    private BigDecimal prepaymentAmount;

    // Before prepayment
    private BigDecimal previousRemainingAmount;
    private BigDecimal previousMonthlyEmi;
    private Integer previousRemainingMonths;

    // After prepayment
    private BigDecimal newRemainingAmount;
    private BigDecimal newMonthlyEmi;
    private Integer newRemainingMonths;

    // Savings
    private BigDecimal interestSaved;

    private String message;
}
