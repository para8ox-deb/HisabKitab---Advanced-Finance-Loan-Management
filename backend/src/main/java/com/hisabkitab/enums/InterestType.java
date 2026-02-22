package com.hisabkitab.enums;

/**
 * Type of interest calculation for a loan.
 *
 * SIMPLE   - Interest on original principal only
 *            Formula: SI = (P × R × T) / 100
 *            EMI = (P + SI) / N  (equal every month)
 *
 * COMPOUND - Interest on principal + accumulated interest (monthly compounding)
 *            Formula: EMI = P × r × (1+r)^n / ((1+r)^n - 1)
 *            Where r = annual rate / 12 / 100 (monthly rate)
 *            Each EMI has different principal/interest split
 */
public enum InterestType {
    SIMPLE,
    COMPOUND
}
