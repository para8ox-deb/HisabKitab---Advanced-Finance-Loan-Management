package com.hisabkitab.service;

import com.hisabkitab.dto.DashboardResponse;
import com.hisabkitab.entity.Loan;
import com.hisabkitab.entity.User;
import com.hisabkitab.enums.BorrowerStatus;
import com.hisabkitab.enums.LoanStatus;
import com.hisabkitab.repository.BorrowerRepository;
import com.hisabkitab.repository.LoanRepository;
import com.hisabkitab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DashboardService - Aggregates data from borrowers, loans, and EMIs
 * into a single unified response for the lender's dashboard.
 *
 * ONE API call = full dashboard data. This is much more efficient than
 * making 5 separate API calls from the frontend.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final BorrowerRepository borrowerRepository;
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new com.hisabkitab.exception.ResourceNotFoundException("User not found!"));
    }

    /**
     * Builds the complete dashboard response.
     *
     * This method aggregates:
     * 1. Borrower counts (total, active)
     * 2. Loan counts (total, active, completed)
     * 3. Financial totals (lent, earned, outstanding, collected)
     * 4. Last 5 created loans
     */
    public DashboardResponse getDashboard() {
        User lender = getCurrentUser();

        // ===== 1. Borrower Stats =====
        long totalBorrowers = borrowerRepository.countByLender(lender);
        long activeBorrowers = borrowerRepository.findByLenderAndStatus(lender, BorrowerStatus.ACTIVE).size();

        // ===== 2. Loan Stats =====
        long totalLoans = loanRepository.countByLender(lender);
        long activeLoans = loanRepository.countByLenderAndStatus(lender, LoanStatus.ACTIVE);
        long completedLoans = loanRepository.countByLenderAndStatus(lender, LoanStatus.COMPLETED);

        // ===== 3. Financial Summary =====
        List<Loan> allLoans = loanRepository.findByLender(lender);

        BigDecimal totalAmountLent = allLoans.stream()
                .map(Loan::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalInterestEarned = allLoans.stream()
                .filter(l -> l.getStatus() == LoanStatus.COMPLETED)
                .map(Loan::getTotalInterest)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOutstanding = allLoans.stream()
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE)
                .map(Loan::getRemainingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCollected = allLoans.stream()
                .map(Loan::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ===== 4. Recent Loans (last 5 by creation date) =====
        List<DashboardResponse.RecentLoan> recentLoans = allLoans.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .map(loan -> DashboardResponse.RecentLoan.builder()
                        .loanId(loan.getId())
                        .borrowerName(loan.getBorrower().getName())
                        .principalAmount(loan.getPrincipalAmount())
                        .interestRate(loan.getInterestRate())
                        .status(loan.getStatus().name())
                        .createdAt(loan.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        // ===== Build Response =====
        return DashboardResponse.builder()
                .totalBorrowers(totalBorrowers)
                .activeBorrowers(activeBorrowers)
                .totalLoans(totalLoans)
                .activeLoans(activeLoans)
                .completedLoans(completedLoans)
                .totalAmountLent(totalAmountLent)
                .totalInterestEarned(totalInterestEarned)
                .totalOutstanding(totalOutstanding)
                .totalCollected(totalCollected)
                .recentLoans(recentLoans)
                .build();
    }
}
