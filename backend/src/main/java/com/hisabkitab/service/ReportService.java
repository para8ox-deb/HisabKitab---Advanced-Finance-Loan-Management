package com.hisabkitab.service;

import com.hisabkitab.entity.Loan;
import com.hisabkitab.entity.User;
import com.hisabkitab.enums.LoanStatus;
import com.hisabkitab.repository.LoanRepository;
import com.hisabkitab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * ReportService — Generates CSV reports for loan data.
 *
 * CSV (Comma-Separated Values) is a simple text format that Excel,
 * Google Sheets, and any spreadsheet app can open directly.
 *
 * Format:
 *   Header1,Header2,Header3
 *   Value1,Value2,Value3
 *   Value4,Value5,Value6
 *
 * This service generates the CSV content as a String.
 * The controller handles setting the HTTP headers for download.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new com.hisabkitab.exception.ResourceNotFoundException("User not found!"));
    }

    /**
     * Generate CSV of ALL loans for the current lender.
     *
     * Columns: Loan ID, Borrower Name, Principal, Interest Rate, Interest Type,
     *          Duration (Months), Monthly EMI, Total Interest, Total Amount,
     *          Paid Amount, Remaining Amount, Status, Start Date, End Date
     */
    public String generateLoanReportCSV() {
        User lender = getCurrentUser();
        List<Loan> loans = loanRepository.findByLender(lender);

        StringBuilder csv = new StringBuilder();

        // Header row
        csv.append("Loan ID,Borrower Name,Principal Amount,Interest Rate (%),Interest Type,")
           .append("Duration (Months),Monthly EMI,Total Interest,Total Amount,")
           .append("Paid Amount,Remaining Amount,Status,Start Date,End Date\n");

        // Data rows
        for (Loan loan : loans) {
            csv.append(loan.getId()).append(",");
            csv.append(escapeCSV(loan.getBorrower().getName())).append(",");
            csv.append(loan.getPrincipalAmount()).append(",");
            csv.append(loan.getInterestRate()).append(",");
            csv.append(loan.getInterestType()).append(",");
            csv.append(loan.getDurationMonths()).append(",");
            csv.append(loan.getMonthlyEmi()).append(",");
            csv.append(loan.getTotalInterest()).append(",");
            csv.append(loan.getTotalAmount()).append(",");
            csv.append(loan.getPaidAmount()).append(",");
            csv.append(loan.getRemainingAmount()).append(",");
            csv.append(loan.getStatus()).append(",");
            csv.append(loan.getStartDate()).append(",");
            csv.append(loan.getEndDate()).append("\n");
        }

        return csv.toString();
    }


    /**
     * Generate a summary report of all loans grouped by status.
     *
     * Shows active, completed, and defaulted loans with totals.
     */
    public String generateSummaryReportCSV() {
        User lender = getCurrentUser();
        List<Loan> loans = loanRepository.findByLender(lender);

        long active = loans.stream().filter(l -> l.getStatus() == LoanStatus.ACTIVE).count();
        long completed = loans.stream().filter(l -> l.getStatus() == LoanStatus.COMPLETED).count();
        long defaulted = loans.stream().filter(l -> l.getStatus() == LoanStatus.DEFAULTED).count();

        BigDecimal totalLent = loans.stream()
                .map(Loan::getPrincipalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalInterestEarned = loans.stream()
                .filter(l -> l.getStatus() == LoanStatus.COMPLETED)
                .map(Loan::getTotalInterest).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalOutstanding = loans.stream()
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE)
                .map(Loan::getRemainingAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCollected = loans.stream()
                .map(Loan::getPaidAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        StringBuilder csv = new StringBuilder();
        csv.append("HisabKitab - Lending Summary Report\n");
        csv.append("Generated for: ").append(lender.getName()).append("\n\n");

        csv.append("Category,Count\n");
        csv.append("Total Loans,").append(loans.size()).append("\n");
        csv.append("Active Loans,").append(active).append("\n");
        csv.append("Completed Loans,").append(completed).append("\n");
        csv.append("Defaulted Loans,").append(defaulted).append("\n\n");

        csv.append("Financial Metric,Amount (Rs)\n");
        csv.append("Total Amount Lent,").append(totalLent).append("\n");
        csv.append("Total Interest Earned (Completed),").append(totalInterestEarned).append("\n");
        csv.append("Total Outstanding (Active),").append(totalOutstanding).append("\n");
        csv.append("Total Collected,").append(totalCollected).append("\n");

        return csv.toString();
    }

    /**
     * Escape special characters in CSV values.
     *
     * If a value contains commas, quotes, or newlines, it must be
     * wrapped in double quotes. Any double quotes inside must be doubled.
     */
    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
