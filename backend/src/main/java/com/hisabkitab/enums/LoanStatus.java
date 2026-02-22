package com.hisabkitab.enums;

/**
 * Tracks the lifecycle of a loan.
 *
 * ACTIVE    - Loan is ongoing, EMIs are being paid
 * COMPLETED - All EMIs paid, loan is fully settled
 * DEFAULTED - Borrower has stopped paying, marked as bad debt
 */
public enum LoanStatus {
    ACTIVE,
    COMPLETED,
    DEFAULTED
}
