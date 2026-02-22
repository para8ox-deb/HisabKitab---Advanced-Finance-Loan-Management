package com.hisabkitab.enums;

/**
 * Status for tracking if a borrower is currently active or not.
 *
 * ACTIVE   - Borrower has active loans or is available for new loans
 * INACTIVE - Borrower has been deactivated (no new loans)
 */
public enum BorrowerStatus {
    ACTIVE,
    INACTIVE
}
