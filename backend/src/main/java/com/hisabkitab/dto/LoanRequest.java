package com.hisabkitab.dto;

import com.hisabkitab.enums.InterestType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating a new loan.
 *
 * Notice: borrowerId is included because the lender selects which borrower.
 * But lenderId is NOT included — it comes from the JWT token.
 * Calculated fields (totalInterest, monthlyEmi, etc.) are NOT included —
 * the server calculates them.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanRequest {

    @NotNull(message = "Borrower ID is required")
    private Long borrowerId;

    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "100.00", message = "Minimum loan amount is ₹100")
    private BigDecimal principalAmount;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.0", message = "Interest rate cannot be negative")
    @DecimalMax(value = "100.0", message = "Interest rate cannot exceed 100%")
    private BigDecimal interestRate;

    @NotNull(message = "Duration in months is required")
    @Min(value = 1, message = "Minimum duration is 1 month")
    @Max(value = 360, message = "Maximum duration is 360 months")
    private Integer durationMonths;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    /**
     * Type of interest: SIMPLE or COMPOUND.
     * If not provided, defaults to SIMPLE.
     */
    private InterestType interestType = InterestType.SIMPLE;
}
