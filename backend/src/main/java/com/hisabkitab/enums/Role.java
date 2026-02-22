package com.hisabkitab.enums;

/**
 * Defines the roles available in the HisabKitab system.
 *
 * ADMIN   - Full system oversight, can manage all users and view all data
 * LENDER  - Can manage borrowers, create loans, record payments
 * BORROWER - Can view their own loans and payment history
 */
public enum Role {
    ADMIN,
    LENDER,
    BORROWER
}
