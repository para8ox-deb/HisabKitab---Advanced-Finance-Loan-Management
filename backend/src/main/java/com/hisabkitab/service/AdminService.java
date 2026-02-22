package com.hisabkitab.service;

import com.hisabkitab.dto.AdminDashboardResponse;
import com.hisabkitab.entity.Borrower;
import com.hisabkitab.entity.Loan;
import com.hisabkitab.entity.User;
import com.hisabkitab.enums.LoanStatus;
import com.hisabkitab.enums.Role;
import com.hisabkitab.repository.LoanRepository;
import com.hisabkitab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AdminService — Business logic for the Admin Dashboard.
 *
 * Unlike Lender/Borrower services, the Admin has NO data isolation.
 * They see ALL data across the entire system — users, loans, borrowers.
 *
 * This is for system oversight: monitoring growth, catching issues,
 * managing user accounts, etc.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final com.hisabkitab.repository.BorrowerRepository borrowerRepository;
    private final com.hisabkitab.repository.LoanScheduleRepository loanScheduleRepository;

    /**
     * GET ADMIN DASHBOARD — System-wide statistics.
     *
     * This method pulls data from multiple repositories and
     * aggregates it into a single response.
     */
    public AdminDashboardResponse getAdminDashboard() {

        // ---- User Statistics ----
        List<User> allUsers = userRepository.findAll();
        long totalUsers = allUsers.size();
        long totalLenders = allUsers.stream().filter(u -> u.getRole() == Role.LENDER).count();
        long totalBorrowerUsers = allUsers.stream().filter(u -> u.getRole() == Role.BORROWER).count();
        long activeUsers = allUsers.stream().filter(User::isActive).count();
        long inactiveUsers = totalUsers - activeUsers;

        // ---- Loan Statistics (across ALL lenders) ----
        List<Loan> allLoans = loanRepository.findAll();
        long totalLoans = allLoans.size();
        long activeLoans = allLoans.stream().filter(l -> l.getStatus() == LoanStatus.ACTIVE).count();
        long completedLoans = allLoans.stream().filter(l -> l.getStatus() == LoanStatus.COMPLETED).count();

        BigDecimal totalPrincipalLent = allLoans.stream()
                .map(Loan::getPrincipalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalInterestGenerated = allLoans.stream()
                .map(Loan::getTotalInterest).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAmountCollected = allLoans.stream()
                .map(Loan::getPaidAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalOutstanding = allLoans.stream()
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE)
                .map(Loan::getRemainingAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        // ---- Recent Users (last 10 registrations) ----
        List<AdminDashboardResponse.UserSummary> recentUsers = allUsers.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(10)
                .map(user -> AdminDashboardResponse.UserSummary.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .active(user.isActive())
                        .createdAt(user.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        // ---- Top Lenders (by number of loans) ----
        Map<User, List<Loan>> loansByLender = allLoans.stream()
                .collect(Collectors.groupingBy(Loan::getLender));

        List<AdminDashboardResponse.LenderSummary> topLenders = loansByLender.entrySet().stream()
                .map(entry -> {
                    User lender = entry.getKey();
                    List<Loan> lenderLoans = entry.getValue();
                    BigDecimal totalLent = lenderLoans.stream()
                            .map(Loan::getPrincipalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    return AdminDashboardResponse.LenderSummary.builder()
                            .id(lender.getId())
                            .name(lender.getName())
                            .email(lender.getEmail())
                            .loanCount(lenderLoans.size())
                            .totalLent(totalLent)
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getLoanCount(), a.getLoanCount()))
                .limit(5)
                .collect(Collectors.toList());

        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalLenders(totalLenders)
                .totalBorrowers(totalBorrowerUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .totalLoans(totalLoans)
                .activeLoans(activeLoans)
                .completedLoans(completedLoans)
                .totalPrincipalLent(totalPrincipalLent)
                .totalInterestGenerated(totalInterestGenerated)
                .totalAmountCollected(totalAmountCollected)
                .totalOutstanding(totalOutstanding)
                .recentUsers(recentUsers)
                .topLenders(topLenders)
                .build();
    }

    /**
     * GET ALL USERS — List all registered users.
     * Admin can see and manage all users in the system.
     */
    public List<AdminDashboardResponse.UserSummary> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> AdminDashboardResponse.UserSummary.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .active(user.isActive())
                        .createdAt(user.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * TOGGLE USER STATUS — Activate or deactivate a user account.
     * Deactivated users cannot log in.
     */
    public AdminDashboardResponse.UserSummary toggleUserStatus(Long userId) {
        String loggedInEmail = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.hisabkitab.exception.ResourceNotFoundException("User not found with id: " + userId));

        if (user.getEmail().equals(loggedInEmail)) {
            throw new IllegalArgumentException("You cannot modify your own admin account status.");
        }

        user.setActive(!user.isActive());
        User updated = userRepository.save(user);

        return AdminDashboardResponse.UserSummary.builder()
                .id(updated.getId())
                .name(updated.getName())
                .email(updated.getEmail())
                .role(updated.getRole())
                .active(updated.isActive())
                .createdAt(updated.getCreatedAt())
                .build();
    }

    /**
     * GET USER DETAILS — Get comprehensive details of a specific user.
     */
    public com.hisabkitab.dto.UserDetailResponse getUserDetails(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.hisabkitab.exception.ResourceNotFoundException("User not found with id: " + userId));

        List<Loan> userLoans = loanRepository.findByLender(user);
        
        long activeLoans = userLoans.stream().filter(l -> l.getStatus() == LoanStatus.ACTIVE).count();
        BigDecimal totalPrincipalLent = userLoans.stream()
                .map(Loan::getPrincipalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalInterestGenerated = userLoans.stream()
                .map(Loan::getTotalInterest).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAmountCollected = userLoans.stream()
                .map(Loan::getPaidAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Borrower> borrowers = borrowerRepository.findByLender(user);
        List<com.hisabkitab.dto.UserDetailResponse.BorrowerSummary> borrowerSummaries = borrowers.stream()
                .map(b -> {
                    List<Loan> bLoans = loanRepository.findByBorrowerAndLender(b, user);
                    int bActiveLoans = (int) bLoans.stream().filter(l -> l.getStatus() == LoanStatus.ACTIVE).count();
                    BigDecimal bTotalOwed = bLoans.stream()
                            .filter(l -> l.getStatus() == LoanStatus.ACTIVE)
                            .map(Loan::getRemainingAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                            
                    return com.hisabkitab.dto.UserDetailResponse.BorrowerSummary.builder()
                            .id(b.getId())
                            .name(b.getName())
                            .phone(b.getPhone())
                            .activeLoanCount(bActiveLoans)
                            .totalOwed(bTotalOwed)
                            .build();
                })
                .collect(Collectors.toList());

        return com.hisabkitab.dto.UserDetailResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .totalLoans(userLoans.size())
                .activeLoans(activeLoans)
                .totalPrincipalLent(totalPrincipalLent)
                .totalInterestGenerated(totalInterestGenerated)
                .totalAmountCollected(totalAmountCollected)
                .borrowers(borrowerSummaries)
                .build();
    }

    /**
     * DELETE USER — Hard delete a user and all associated data.
     */
    @org.springframework.transaction.annotation.Transactional
    public void deleteUser(Long userId) {
        String loggedInEmail = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.hisabkitab.exception.ResourceNotFoundException("User not found with id: " + userId));

        if (user.getEmail().equals(loggedInEmail)) {
            throw new IllegalStateException("You cannot delete your own admin account.");
        }

        // 1. Unlink if this user is a borrower portal account
        java.util.Optional<Borrower> linkedBorrowerOpt = borrowerRepository.findByLinkedUser(user);
        if (linkedBorrowerOpt.isPresent()) {
            Borrower b = linkedBorrowerOpt.get();
            b.setLinkedUser(null);
            borrowerRepository.save(b);
        }

        // 2. Delete all loans for this lender (and their schedules first to avoid FK constraints)
        List<Loan> lenderLoans = loanRepository.findByLender(user);
        for (Loan loan : lenderLoans) {
            loanScheduleRepository.deleteByLoan(loan);
        }
        loanRepository.deleteByLender(user);

        // 3. Delete all borrowers for this lender
        borrowerRepository.deleteByLender(user);

        // 4. Finally delete the user
        userRepository.delete(user);
    }

    /**
     * DELETE BORROWER — Hard delete a borrower and all associated loans.
     */
    @org.springframework.transaction.annotation.Transactional
    public void deleteBorrower(Long borrowerId) {
        Borrower borrower = borrowerRepository.findById(borrowerId)
                .orElseThrow(() -> new com.hisabkitab.exception.ResourceNotFoundException("Borrower not found with id: " + borrowerId));

        // 2. Delete all loans for this borrower (and their schedules first)
        List<Loan> borrowerLoans = loanRepository.findByBorrower(borrower);
        for (Loan loan : borrowerLoans) {
            loanScheduleRepository.deleteByLoan(loan);
        }
        loanRepository.deleteByBorrower(borrower);

        // 3. Finally delete the borrower
        borrowerRepository.delete(borrower);
    }
}
