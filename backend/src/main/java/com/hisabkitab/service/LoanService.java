package com.hisabkitab.service;

import com.hisabkitab.dto.LoanRequest;
import com.hisabkitab.dto.LoanResponse;
import com.hisabkitab.dto.PrepaymentRequest;
import com.hisabkitab.dto.PrepaymentResponse;
import com.hisabkitab.entity.Borrower;
import com.hisabkitab.entity.Loan;
import com.hisabkitab.entity.User;
import com.hisabkitab.enums.BorrowerStatus;
import com.hisabkitab.enums.InterestType;
import com.hisabkitab.enums.LoanStatus;
import com.hisabkitab.entity.LoanSchedule;
import com.hisabkitab.dto.LoanScheduleResponse;
import com.hisabkitab.dto.SchedulePaymentRequest;
import com.hisabkitab.enums.PaymentStatus;
import com.hisabkitab.repository.BorrowerRepository;
import com.hisabkitab.repository.LoanRepository;
import com.hisabkitab.repository.LoanScheduleRepository;
import com.hisabkitab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;

/**
 * LoanService - Core business logic for loan management.
 *
 * Responsibilities:
 * 1. Create loans with simple OR compound interest calculation
 * 2. Auto-generate monthly EMI schedule (flat for SI, amortized for CI)
 * 3. Record EMI payments
 * 4. Track loan status and payment progress
 *
 * @Transactional ensures that if ANY step fails during loan creation
 * (e.g., EMI generation fails), the ENTIRE operation is rolled back.
 */
@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final BorrowerRepository borrowerRepository;
    private final UserRepository userRepository;
    private final LoanScheduleRepository loanScheduleRepository;

    @PostConstruct
    @Transactional
    public void backfillSchedulesForExistingLoans() {
        System.out.println("Starting Schedule Backfill Check...");
        List<Loan> allLoans = loanRepository.findAll();
        for (Loan loan : allLoans) {
            if (loan.getStatus() == LoanStatus.ACTIVE && loanScheduleRepository.findByLoanOrderByMonthNumberAsc(loan).isEmpty()) {
                System.out.println("Backfilling exactly " + loan.getDurationMonths() + " schedules for legacy Loan ID: " + loan.getId());
                for (int i = 1; i <= loan.getDurationMonths(); i++) {
                    LoanSchedule schedule = LoanSchedule.builder()
                            .loan(loan)
                            .monthNumber(i)
                            .dueDate(loan.getStartDate().plusMonths(i))
                            .amountDue(loan.getMonthlyEmi())
                            .status(PaymentStatus.PENDING)
                            .build();
                    loanScheduleRepository.save(schedule);
                }
            }
        }
        System.out.println("Schedule Backfill Complete.");
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new com.hisabkitab.exception.ResourceNotFoundException("User not found!"));
    }

    // ==================== Mapper Methods ====================

    private LoanResponse toLoanResponse(Loan loan) {
        List<LoanScheduleResponse> schedules = loanScheduleRepository.findByLoanOrderByMonthNumberAsc(loan)
                .stream()
                .map(s -> LoanScheduleResponse.builder()
                        .id(s.getId())
                        .loanId(loan.getId())
                        .monthNumber(s.getMonthNumber())
                        .dueDate(s.getDueDate())
                        .amountDue(s.getAmountDue())
                        .status(s.getStatus())
                        .paidDate(s.getPaidDate())
                        .note(s.getNote())
                        .build())
                .collect(Collectors.toList());

        return LoanResponse.builder()
                .id(loan.getId())
                .borrowerId(loan.getBorrower().getId())
                .borrowerName(loan.getBorrower().getName())
                .principalAmount(loan.getPrincipalAmount())
                .interestRate(loan.getInterestRate())
                .durationMonths(loan.getDurationMonths())
                .interestType(loan.getInterestType())
                .startDate(loan.getStartDate())
                .endDate(loan.getEndDate())
                .totalInterest(loan.getTotalInterest())
                .totalAmount(loan.getTotalAmount())
                .monthlyEmi(loan.getMonthlyEmi())
                .paidAmount(loan.getPaidAmount())
                .remainingAmount(loan.getRemainingAmount())
                .status(loan.getStatus())
                .createdAt(loan.getCreatedAt())
                .schedules(schedules)
                .build();
    }


    // ==================== Loan CRUD ====================

    /**
     * CREATE LOAN - The most important method.
     *
     * Steps:
     * 1. Validate borrower exists and belongs to current lender
     * 2. Calculate simple interest
     * 3. Calculate monthly EMI
     * 4. Save the loan
     *
     * @Transactional
     */
    @Transactional
    public LoanResponse createLoan(LoanRequest request) {
        User lender = getCurrentUser();

        // 1. Validate borrower belongs to this lender
        Borrower borrower = borrowerRepository.findByIdAndLender(request.getBorrowerId(), lender)
                .orElseThrow(() -> new com.hisabkitab.exception.ResourceNotFoundException("Borrower not found with id: " + request.getBorrowerId()));

        // Check if borrower is active
        if (borrower.getStatus() != BorrowerStatus.ACTIVE) {
            throw new IllegalStateException("Cannot create loan for inactive borrower!");
        }

        BigDecimal principal = request.getPrincipalAmount();
        BigDecimal rate = request.getInterestRate();
        int months = request.getDurationMonths();
        InterestType interestType = request.getInterestType();

        BigDecimal totalInterest;
        BigDecimal totalAmount;
        BigDecimal monthlyEmi;

        if (interestType == InterestType.COMPOUND) {
            // ================================================
            // TRUE COMPOUND INTEREST (Chakravarti Vyaj)
            // ================================================
            // In a private lending context where only interest is scheduled,
            // Compound Interest yields MORE than Simple Interest because
            // the schedule assumes interest compounds mathematically.
            // P_new = P_old * (1 + r)
            // Interest for month = P_new * r
            // ================================================
            BigDecimal monthlyRate = rate.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
            BigDecimal currentPrincipalForMath = principal;
            BigDecimal sumInterest = BigDecimal.ZERO;
            
            // Build the static theoretical schedule
            List<LoanSchedule> schedulesToSave = new ArrayList<>();
            for (int i = 1; i <= months; i++) {
                BigDecimal interestForMonth = currentPrincipalForMath.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
                sumInterest = sumInterest.add(interestForMonth);
                
                // Add to principal for next month's calculation (Compounding effect)
                currentPrincipalForMath = currentPrincipalForMath.add(interestForMonth);
                
                LoanSchedule schedule = LoanSchedule.builder()
                        // loan reference will be set after entity is saved
                        .monthNumber(i)
                        .dueDate(request.getStartDate().plusMonths(i))
                        .amountDue(interestForMonth)
                        .status(PaymentStatus.PENDING)
                        .build();
                schedulesToSave.add(schedule);
            }

            totalInterest = sumInterest;
            totalAmount = principal.add(totalInterest);
            monthlyEmi = sumInterest.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP); // Just an average for stats, UI ignores it for Compound

            // Calculate end date
            LocalDate endDate = request.getStartDate().plusMonths(months);

            Loan loan = Loan.builder()
                    .lender(lender)
                    .borrower(borrower)
                    .principalAmount(principal)
                    .interestRate(rate)
                    .durationMonths(months)
                    .interestType(interestType)
                    .startDate(request.getStartDate())
                    .endDate(endDate)
                    .totalInterest(totalInterest)
                    .totalAmount(totalAmount)
                    .monthlyEmi(monthlyEmi)
                    .paidAmount(BigDecimal.ZERO)
                    .remainingAmount(totalAmount)
                    .status(LoanStatus.ACTIVE)
                    .build();

            Loan savedLoan = loanRepository.save(loan);

            for (LoanSchedule s : schedulesToSave) {
                s.setLoan(savedLoan);
                loanScheduleRepository.save(s);
            }

            return toLoanResponse(savedLoan);

        } else {
            // ================================================
            // SIMPLE INTEREST — Sekda (Monthly) formula
            // ================================================
            // SI = (P × R × T) / 100
            // ================================================
            totalInterest = principal
                    .multiply(rate)
                    .multiply(BigDecimal.valueOf(months))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            totalAmount = principal.add(totalInterest);

            // Monthly payment = INTEREST ONLY
            monthlyEmi = totalInterest
                    .divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
            
            LocalDate endDate = request.getStartDate().plusMonths(months);

            Loan loan = Loan.builder()
                    .lender(lender)
                    .borrower(borrower)
                    .principalAmount(principal)
                    .interestRate(rate)
                    .durationMonths(months)
                    .interestType(interestType)
                    .startDate(request.getStartDate())
                    .endDate(endDate)
                    .totalInterest(totalInterest)
                    .totalAmount(totalAmount)
                    .monthlyEmi(monthlyEmi)
                    .paidAmount(BigDecimal.ZERO)
                    .remainingAmount(totalAmount)
                    .status(LoanStatus.ACTIVE)
                    .build();

            Loan savedLoan = loanRepository.save(loan);

            for (int i = 1; i <= months; i++) {
                LoanSchedule schedule = LoanSchedule.builder()
                        .loan(savedLoan)
                        .monthNumber(i)
                        .dueDate(request.getStartDate().plusMonths(i))
                        .amountDue(monthlyEmi)
                        .status(PaymentStatus.PENDING)
                        .build();
                loanScheduleRepository.save(schedule);
            }

            return toLoanResponse(savedLoan);
        }
    }

    /**
     * UPDATE LOAN - Edit loan terms mid-cycle.
     */
    @Transactional
    public LoanResponse updateLoan(Long loanId, LoanRequest request) {
        User lender = getCurrentUser();
        Loan loan = loanRepository.findByIdAndLender(loanId, lender)
                .orElseThrow(() -> new com.hisabkitab.exception.ResourceNotFoundException("Loan not found with id: " + loanId));

        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new IllegalStateException("Cannot edit a non-active loan");
        }

        Borrower borrower = borrowerRepository.findByIdAndLender(request.getBorrowerId(), lender)
                .orElseThrow(() -> new com.hisabkitab.exception.ResourceNotFoundException("Borrower not found"));

        loan.setBorrower(borrower);
        loan.setPrincipalAmount(request.getPrincipalAmount());
        loan.setInterestRate(request.getInterestRate());
        loan.setInterestType(request.getInterestType());
        loan.setStartDate(request.getStartDate());

        int newMonths = request.getDurationMonths();
        loan.setDurationMonths(newMonths);
        loan.setEndDate(request.getStartDate().plusMonths(newMonths));

        List<LoanSchedule> existingSchedules = loanScheduleRepository.findByLoanOrderByMonthNumberAsc(loan);
        int paidCount = (int) existingSchedules.stream().filter(s -> s.getStatus() == PaymentStatus.PAID).count();

        if (newMonths < paidCount) {
            throw new IllegalArgumentException("Cannot reduce duration below the number of already paid months (" + paidCount + ")");
        }

        if (newMonths < existingSchedules.size()) {
            List<LoanSchedule> toDelete = new ArrayList<>(existingSchedules.subList(newMonths, existingSchedules.size()));
            loanScheduleRepository.deleteAll(toDelete);
            existingSchedules = new ArrayList<>(existingSchedules.subList(0, newMonths));
        } else if (newMonths > existingSchedules.size()) {
            int currentSize = existingSchedules.size();
            for (int i = currentSize + 1; i <= newMonths; i++) {
                LoanSchedule newSchedule = LoanSchedule.builder()
                        .loan(loan)
                        .monthNumber(i)
                        .dueDate(request.getStartDate().plusMonths(i))
                        .status(PaymentStatus.PENDING)
                        .build();
                existingSchedules.add(newSchedule);
            }
        }

        BigDecimal totalInterest;
        BigDecimal totalAmount;
        BigDecimal monthlyEmi;

        if (loan.getInterestType() == InterestType.COMPOUND) {
            BigDecimal monthlyRate = loan.getInterestRate().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
            BigDecimal currentPrincipalForMath = loan.getPrincipalAmount();
            BigDecimal sumInterest = BigDecimal.ZERO;

            for (int i = 0; i < newMonths; i++) {
                LoanSchedule s = existingSchedules.get(i);
                BigDecimal interestForMonth = currentPrincipalForMath.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
                sumInterest = sumInterest.add(interestForMonth);
                currentPrincipalForMath = currentPrincipalForMath.add(interestForMonth);

                s.setAmountDue(interestForMonth);
                s.setDueDate(loan.getStartDate().plusMonths(s.getMonthNumber()));
                loanScheduleRepository.save(s);
            }
            totalInterest = sumInterest;
            totalAmount = loan.getPrincipalAmount().add(totalInterest);
            monthlyEmi = sumInterest.divide(BigDecimal.valueOf(newMonths), 2, RoundingMode.HALF_UP);
        } else {
            totalInterest = loan.getPrincipalAmount()
                    .multiply(loan.getInterestRate())
                    .multiply(BigDecimal.valueOf(newMonths))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            totalAmount = loan.getPrincipalAmount().add(totalInterest);
            monthlyEmi = totalInterest.divide(BigDecimal.valueOf(newMonths), 2, RoundingMode.HALF_UP);

            for (int i = 0; i < newMonths; i++) {
                LoanSchedule s = existingSchedules.get(i);
                s.setAmountDue(monthlyEmi);
                s.setDueDate(loan.getStartDate().plusMonths(s.getMonthNumber()));
                loanScheduleRepository.save(s);
            }
        }

        BigDecimal newlyCalculatedPaidAmount = existingSchedules.stream()
                .filter(s -> s.getStatus() == PaymentStatus.PAID)
                .map(LoanSchedule::getAmountDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        loan.setTotalInterest(totalInterest);
        loan.setTotalAmount(totalAmount);
        loan.setMonthlyEmi(monthlyEmi);
        loan.setPaidAmount(newlyCalculatedPaidAmount);
        loan.setRemainingAmount(totalAmount.subtract(newlyCalculatedPaidAmount));

        Loan savedLoan = loanRepository.save(loan);
        return toLoanResponse(savedLoan);
    }

    /**
     * GET ALL LOANS for the current lender.
     */
    public List<LoanResponse> getAllLoans() {
        User lender = getCurrentUser();
        return loanRepository.findByLender(lender).stream()
                .map(this::toLoanResponse)
                .collect(Collectors.toList());
    }

    /**
     * GET LOAN BY ID.
     */
    public LoanResponse getLoanById(Long id) {
        User lender = getCurrentUser();
        Loan loan = loanRepository.findByIdAndLender(id, lender)
                .orElseThrow(() -> new com.hisabkitab.exception.ResourceNotFoundException("Loan not found with id: " + id));
        return toLoanResponse(loan);
    }

    /**
     * GET LOANS BY STATUS (ACTIVE, COMPLETED, DEFAULTED).
     */
    public List<LoanResponse> getLoansByStatus(LoanStatus status) {
        User lender = getCurrentUser();
        return loanRepository.findByLenderAndStatus(lender, status).stream()
                .map(this::toLoanResponse)
                .collect(Collectors.toList());
    }

    /**
     * GET LOANS FOR A SPECIFIC BORROWER.
     */
    public List<LoanResponse> getLoansByBorrower(Long borrowerId) {
        User lender = getCurrentUser();
        Borrower borrower = borrowerRepository.findByIdAndLender(borrowerId, lender)
                .orElseThrow(() -> new com.hisabkitab.exception.ResourceNotFoundException("Borrower not found with id: " + borrowerId));
        return loanRepository.findByBorrowerAndLender(borrower, lender).stream()
                .map(this::toLoanResponse)
                .collect(Collectors.toList());
    }



    /**
     * GET LOAN SUMMARY STATS for the dashboard.
     */
    public LoanSummary getLoanSummary() {
        User lender = getCurrentUser();
        long totalLoans = loanRepository.countByLender(lender);
        long activeLoans = loanRepository.countByLenderAndStatus(lender, LoanStatus.ACTIVE);
        long completedLoans = loanRepository.countByLenderAndStatus(lender, LoanStatus.COMPLETED);

        // Calculate totals from all loans
        List<Loan> allLoans = loanRepository.findByLender(lender);
        BigDecimal totalLent = allLoans.stream()
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

        return LoanSummary.builder()
                .totalLoans(totalLoans)
                .activeLoans(activeLoans)
                .completedLoans(completedLoans)
                .totalLent(totalLent)
                .totalInterestEarned(totalInterestEarned)
                .totalOutstanding(totalOutstanding)
                .build();
    }

    /**
     * Inner class for loan summary stats.
     */
    /**
     * Mark a specific schedule month as PAID and attach a note.
     */
    @Transactional
    public LoanScheduleResponse paySchedule(Long scheduleId, SchedulePaymentRequest request) {
        User lender = getCurrentUser();
        LoanSchedule schedule = loanScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new com.hisabkitab.exception.ResourceNotFoundException("Schedule not found with id: " + scheduleId));

        if (!schedule.getLoan().getLender().getId().equals(lender.getId())) {
            throw new com.hisabkitab.exception.UnauthorizedException("Unauthorized: You do not own this loan schedule");
        }
        
        if (schedule.getStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("This schedule is already paid");
        }

        schedule.setStatus(PaymentStatus.PAID);
        schedule.setPaidDate(LocalDate.now());
        schedule.setNote(request.getNote());

        LoanSchedule saved = loanScheduleRepository.save(schedule);

        // We also need to add this to the total paid amount of the loan
        Loan loan = schedule.getLoan();
        BigDecimal currentPaid = loan.getPaidAmount() == null ? BigDecimal.ZERO : loan.getPaidAmount();
        loan.setPaidAmount(currentPaid.add(schedule.getAmountDue()));
        BigDecimal remaining = loan.getTotalAmount().subtract(loan.getPaidAmount());
        if (remaining.compareTo(BigDecimal.ZERO) < 0) remaining = BigDecimal.ZERO;
        loan.setRemainingAmount(remaining);
        // Removed the pendingCount == 0 check. 
        // A loan is only completed when the principal is paid off via settleLoan or prepayLoan.
        loanRepository.save(loan);

        return LoanScheduleResponse.builder()
                .id(saved.getId())
                .loanId(loan.getId())
                .monthNumber(saved.getMonthNumber())
                .dueDate(saved.getDueDate())
                .amountDue(saved.getAmountDue())
                .status(saved.getStatus())
                .paidDate(saved.getPaidDate())
                .note(saved.getNote())
                .build();
    }

    /**
     * Handle Partial Prepayment (Sekda model)
     * Reduces principal and recalculates interest dynamically based on exact days elapsed.
     */
    @Transactional
    public com.hisabkitab.dto.PrepaymentResponse prepayLoan(Long loanId, com.hisabkitab.dto.PrepaymentRequest request) {
        User lender = getCurrentUser();
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new com.hisabkitab.exception.ResourceNotFoundException("Loan not found with id: " + loanId));

        if (!loan.getLender().getId().equals(lender.getId())) {
            throw new com.hisabkitab.exception.UnauthorizedException("Unauthorized: You do not own this loan");
        }

        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new IllegalStateException("Cannot prepay a loan that is not ACTIVE");
        }

        BigDecimal prepaymentAmount = request.getAmount();
        BigDecimal oldPrincipal = loan.getPrincipalAmount();

        if (prepaymentAmount.compareTo(oldPrincipal) >= 0) {
            throw new IllegalArgumentException("Prepayment amount cannot be greater than or equal to current principal. Use 'Settle Loan' instead.");
        }

        // Previous state for response
        BigDecimal previousRemaining = loan.getRemainingAmount();
        BigDecimal previousMonthlyEmi = loan.getMonthlyEmi();
        BigDecimal oldTotalInterest = loan.getTotalInterest();

        // 1. Calculate new principal
        BigDecimal newPrincipal = oldPrincipal.subtract(prepaymentAmount);

        // 2. Calculate new schedules for future pending months based on Interest Type
        List<LoanSchedule> allSchedules = loanScheduleRepository.findByLoanOrderByMonthNumberAsc(loan);
        List<LoanSchedule> pendingSchedules = allSchedules.stream()
                .filter(s -> s.getStatus() == PaymentStatus.PENDING)
                .collect(Collectors.toList());

        BigDecimal monthlyRatePerc = loan.getInterestRate();
        BigDecimal newMonthlyEmi;

        if (loan.getInterestType() == InterestType.COMPOUND) {
            BigDecimal monthlyRate = monthlyRatePerc.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
            BigDecimal currentPrincipalForMath = newPrincipal;
            BigDecimal sumInterest = BigDecimal.ZERO;

            for (LoanSchedule s : pendingSchedules) {
                BigDecimal interestForMonth = currentPrincipalForMath.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
                sumInterest = sumInterest.add(interestForMonth);
                
                // Add to principal for next month's calculation (Compounding effect)
                currentPrincipalForMath = currentPrincipalForMath.add(interestForMonth);
                
                s.setAmountDue(interestForMonth);
                loanScheduleRepository.save(s);
            }
            newMonthlyEmi = pendingSchedules.isEmpty() ? BigDecimal.ZERO : sumInterest.divide(BigDecimal.valueOf(pendingSchedules.size()), 2, RoundingMode.HALF_UP);
        } else {
            // Simple Interest (Sekda)
            newMonthlyEmi = newPrincipal.multiply(monthlyRatePerc)
                                             .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            for (LoanSchedule s : pendingSchedules) {
                s.setAmountDue(newMonthlyEmi);
                loanScheduleRepository.save(s);
            }
        }

        // 4. Calculate actual Total Interest across the lifetime of the loan
        // by simply summing the actual amounts of all paid and pending schedules.
        BigDecimal newTotalInterest = allSchedules.stream()
                .map(LoanSchedule::getAmountDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. Update Paid Amount
        BigDecimal currentPaid = loan.getPaidAmount() == null ? BigDecimal.ZERO : loan.getPaidAmount();
        BigDecimal newPaidAmount = currentPaid.add(prepaymentAmount);

        // 6. Calculate Remaining Amount natively from the UI's perspective
        // Remaining = Remaining Principal + Sum of Pending Schedules
        BigDecimal pendingSchedulesSum = allSchedules.stream()
                .filter(s -> s.getStatus() == PaymentStatus.PENDING)
                .map(LoanSchedule::getAmountDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal newRemainingAmount = newPrincipal.add(pendingSchedulesSum);

        // 7. Reconcile Total Amount
        BigDecimal newTotalAmount = newPaidAmount.add(newRemainingAmount);

        // Calculate interest saved for the response
        BigDecimal interestSaved = oldTotalInterest.subtract(newTotalInterest);
        if (interestSaved.compareTo(BigDecimal.ZERO) < 0) interestSaved = BigDecimal.ZERO;

        // 8. Save new values
        loan.setPrincipalAmount(newPrincipal.setScale(2, RoundingMode.HALF_UP));
        loan.setTotalInterest(newTotalInterest.setScale(2, RoundingMode.HALF_UP));
        loan.setMonthlyEmi(newMonthlyEmi.setScale(2, RoundingMode.HALF_UP));
        loan.setTotalAmount(newTotalAmount.setScale(2, RoundingMode.HALF_UP));
        loan.setPaidAmount(newPaidAmount.setScale(2, RoundingMode.HALF_UP));
        loan.setRemainingAmount(newRemainingAmount.setScale(2, RoundingMode.HALF_UP));

        try {
            loanRepository.save(loan);
            // Force flush so that DB constraints are evaluated NOW, not outside the block
            loanRepository.flush(); 
        } catch (Exception e) {
            String errorMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            throw new IllegalStateException("DB Save Error: " + errorMsg);
        }

        return com.hisabkitab.dto.PrepaymentResponse.builder()
                .loanId(loan.getId())
                .prepaymentAmount(prepaymentAmount)
                .previousRemainingAmount(previousRemaining)
                .previousMonthlyEmi(previousMonthlyEmi)
                .newRemainingAmount(loan.getRemainingAmount())
                .newMonthlyEmi(loan.getMonthlyEmi())
                .interestSaved(interestSaved)
                .message("Prepayment processed successfully. Interest math recalculated.")
                .build();
    }

    /**
     * Settle a loan completely.
     * Pays off the remaining balance and changes status to COMPLETED.
     */
    @Transactional
    public void settleLoan(Long loanId) {
        User lender = getCurrentUser();
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new com.hisabkitab.exception.ResourceNotFoundException("Loan not found with id: " + loanId));

        if (!loan.getLender().getId().equals(lender.getId())) {
            throw new com.hisabkitab.exception.UnauthorizedException("Unauthorized: You do not own this loan");
        }

        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new IllegalStateException("Can only settle an ACTIVE loan");
        }

        // Set future PENDING schedules' amountDue to 0 (forgive future interest), then mark as PAID
        List<LoanSchedule> schedules = loanScheduleRepository.findByLoanOrderByMonthNumberAsc(loan);
        for (LoanSchedule s : schedules) {
            if (s.getStatus() == PaymentStatus.PENDING) {
                if (s.getDueDate().isAfter(LocalDate.now())) {
                    s.setAmountDue(BigDecimal.ZERO);
                    s.setStatus(PaymentStatus.PAID);
                    s.setPaidDate(LocalDate.now());
                    s.setNote("Interest forgiven - Settled early");
                } else {
                    s.setStatus(PaymentStatus.PAID);
                    s.setPaidDate(LocalDate.now());
                    s.setNote("Past due interest paid during settlement");
                }
                loanScheduleRepository.save(s);
            }
        }

        // Recalculate true Total Interest based only on what was actually charged
        BigDecimal trueTotalInterest = schedules.stream()
                .map(LoanSchedule::getAmountDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal trueTotalAmount = loan.getPrincipalAmount().add(trueTotalInterest);

        // Update Loan totals
        loan.setTotalInterest(trueTotalInterest);
        loan.setTotalAmount(trueTotalAmount);
        loan.setPaidAmount(trueTotalAmount); // Settled means fully paid
        loan.setRemainingAmount(BigDecimal.ZERO);
        loan.setStatus(LoanStatus.COMPLETED);

        loanRepository.save(loan);
    }

    /**
     * Delete a loan and all its associated EMIs.
     */
    @Transactional
    public void deleteLoan(Long loanId) {
        User lender = getCurrentUser();
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new com.hisabkitab.exception.ResourceNotFoundException("Loan not found with id: " + loanId));

        if (!loan.getLender().getId().equals(lender.getId())) {
            throw new com.hisabkitab.exception.UnauthorizedException("Unauthorized: You do not own this loan");
        }

        loanScheduleRepository.deleteByLoan(loan);

        // Finally, delete the parent loan
        loanRepository.delete(loan);
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class LoanSummary {
        private long totalLoans;
        private long activeLoans;
        private long completedLoans;
        private BigDecimal totalLent;
        private BigDecimal totalInterestEarned;
        private BigDecimal totalOutstanding;
    }
}
