package com.hisabkitab.service;

import com.hisabkitab.dto.BorrowerPortalResponse;
import com.hisabkitab.entity.Borrower;
import com.hisabkitab.entity.Loan;
import com.hisabkitab.entity.User;
import com.hisabkitab.enums.LoanStatus;
import com.hisabkitab.repository.BorrowerRepository;
import com.hisabkitab.repository.LoanRepository;
import com.hisabkitab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BorrowerPortalService — Logic for the Borrower Portal.
 *
 * The Borrower Portal is the BORROWER-facing side of the app.
 * While lenders manage loans, borrowers can only VIEW:
 *   - Their loan details
 *   - Their payment history
 *
 * Key difference from Lender services:
 *   - Lender sees all borrowers and their loans
 *   - Borrower sees only their own loans from ALL lenders
 */
@Service
@RequiredArgsConstructor
public class BorrowerPortalService {

    private final BorrowerRepository borrowerRepository;
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get the currently logged-in User from the JWT token.
     */
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new com.hisabkitab.exception.ResourceNotFoundException("User not found!"));
    }

    /**
     * Find the Borrower record linked to the current logged-in user.
     *
     * The flow: User logs in → JWT has their email → We find User →
     * We find which Borrower has linkedUser = this User.
     */
    private Borrower getCurrentBorrower() {
        User user = getCurrentUser();
        return borrowerRepository.findByLinkedUser(user)
                .orElseThrow(() -> new com.hisabkitab.exception.ResourceNotFoundException(
                        "No borrower profile linked to your account. "
                        + "Ask your lender to link your account."));
    }

    /**
     * GET BORROWER DASHBOARD — Full portal view.
     *
     * Returns:
     *   - Borrower's personal info
     *   - Summary stats (total borrowed, paid, outstanding)
     *   - All their loans with details
     */
    public BorrowerPortalResponse getPortalDashboard() {
        Borrower borrower = getCurrentBorrower();

        // Get all loans for this borrower (from any lender)
        List<Loan> loans = loanRepository.findByBorrower(borrower);

        // Calculate summary stats
        int totalLoans = loans.size();
        int activeLoans = (int) loans.stream()
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE).count();
        int completedLoans = (int) loans.stream()
                .filter(l -> l.getStatus() == LoanStatus.COMPLETED).count();

        BigDecimal totalBorrowed = loans.stream()
                .map(Loan::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaid = loans.stream()
                .map(Loan::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOutstanding = loans.stream()
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE)
                .map(Loan::getRemainingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Build loan views (from borrower's perspective — shows lender name)
        List<BorrowerPortalResponse.BorrowerLoanView> loanViews = loans.stream()
                .map(loan -> BorrowerPortalResponse.BorrowerLoanView.builder()
                        .loanId(loan.getId())
                        .lenderName(loan.getLender().getName())
                        .principalAmount(loan.getPrincipalAmount())
                        .interestRate(loan.getInterestRate())
                        .interestType(loan.getInterestType())
                        .durationMonths(loan.getDurationMonths())
                        .monthlyEmi(loan.getMonthlyEmi())
                        .totalAmount(loan.getTotalAmount())
                        .paidAmount(loan.getPaidAmount())
                        .remainingAmount(loan.getRemainingAmount())
                        .status(loan.getStatus())
                        .startDate(loan.getStartDate())
                        .endDate(loan.getEndDate())
                        .build())
                .collect(Collectors.toList());

        return BorrowerPortalResponse.builder()
                .borrowerName(borrower.getName())
                .phone(borrower.getPhone())
                .address(borrower.getAddress())
                .totalLoans(totalLoans)
                .activeLoans(activeLoans)
                .completedLoans(completedLoans)
                .totalBorrowed(totalBorrowed)
                .totalPaid(totalPaid)
                .totalOutstanding(totalOutstanding)
                .loans(loanViews)
                .build();
    }

    /**
     * UPDATE BORROWER PROFILE
     *
     * Allows the borrower to update their personal info.
     * Optionally updates the password if provided.
     */
    public void updateMyProfile(com.hisabkitab.dto.BorrowerProfileUpdateRequest request) {
        Borrower borrower = getCurrentBorrower();
        User user = borrower.getLinkedUser();

        // Update Borrower info
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            borrower.setName(request.getName().trim());
        }
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            borrower.setPhone(request.getPhone().trim());
        }
        if (request.getAddress() != null) {
            borrower.setAddress(request.getAddress().trim());
        }
        
        borrowerRepository.save(borrower);

        // Update User info (name sync and password)
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }
        if (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty()) {
            // Usually we'd inject PasswordEncoder, for now we will assume the caller or security config handles it, or inject it.
            // Wait, we need to inject PasswordEncoder to hash the new password.
            // I will add the PasswordEncoder dependency to the constructor.
            // Actually, I'll modify the constructor to include PasswordEncoder in a separate step if needed, or get it from context.
            // Oh right, this class uses @RequiredArgsConstructor, so declaring it as final will inject it automatically.
            user.setPassword(passwordEncoder.encode(request.getNewPassword().trim()));
        }
        userRepository.save(user);
    }
}
