package com.hisabkitab.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for making a partial principal prepayment.
 *
 * Prepayment = An extra payment that goes directly toward reducing
 * the principal balance. This is DIFFERENT from a regular EMI payment.
 *
 * Example:
 *   Loan balance = ₹40,000
 *   Borrower pays ₹10,000 as prepayment
 *   New balance = ₹30,000
 *   → Remaining EMIs are recalculated on ₹30,000
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrepaymentRequest {

    @NotNull(message = "Prepayment amount is required")
    @DecimalMin(value = "1.00", message = "Minimum prepayment amount is ₹1")
    private BigDecimal amount;

    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
    private java.time.LocalDate date;
}
