package com.hisabkitab.entity;

import com.hisabkitab.enums.InterestType;
import com.hisabkitab.enums.LoanStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Loan entity - Maps to the "loans" table in MySQL.
 *
 * Represents a loan given by a lender to a borrower.
 *
 * Relationships:
 *   - Many loans → One lender (User)
 *   - Many loans → One borrower
 *
 * Financial fields use BigDecimal (not double) because:
 *   double: 0.1 + 0.2 = 0.30000000000000004  ← BAD for money!
 *   BigDecimal: 0.1 + 0.2 = 0.3              ← Exact precision!
 */
@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The lender who gave this loan.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lender_id", nullable = false)
    private User lender;

    /**
     * The borrower who received this loan.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrower_id", nullable = false)
    private Borrower borrower;

    /**
     * The principal amount of the loan (the original amount lent).
     * Example: ₹50,000
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal principalAmount;

    /**
     * Annual interest rate in percentage.
     * Example: 12.0 means 12% per year
     */
    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.0", message = "Interest rate cannot be negative")
    @DecimalMax(value = "100.0", message = "Interest rate cannot exceed 100%")
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    /**
     * Loan duration in months.
     * Example: 12 means 1 year
     */
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Minimum duration is 1 month")
    @Max(value = 360, message = "Maximum duration is 360 months (30 years)")
    @Column(nullable = false)
    private Integer durationMonths;

    /**
     * Type of interest: SIMPLE or COMPOUND.
     * Defaults to SIMPLE for backward compatibility.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterestType interestType = InterestType.SIMPLE;

    /**
     * When the loan starts (first EMI due date is one month after this).
     */
    @NotNull(message = "Start date is required")
    @Column(nullable = false)
    private LocalDate startDate;

    /**
     * Calculated end date: startDate + durationMonths.
     */
    @Column(nullable = false)
    private LocalDate endDate;

    // ========== Calculated Fields (set by service, not by client) ==========

    /**
     * Total interest over the entire loan period.
     * Simple Interest = (Principal × Rate × Time) / 100
     * Time = durationMonths / 12 (converted to years)
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalInterest;

    /**
     * Total amount to be repaid = Principal + Total Interest.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    /**
     * Monthly EMI = Total Amount / Duration Months.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyEmi;

    /**
     * Total amount actually paid so far (sum of all paid EMIs).
     */
    @Column(precision = 12, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    /**
     * Remaining balance = Total Amount - Paid Amount.
     */
    @Column(precision = 12, scale = 2)
    private BigDecimal remainingAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status = LoanStatus.ACTIVE;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
