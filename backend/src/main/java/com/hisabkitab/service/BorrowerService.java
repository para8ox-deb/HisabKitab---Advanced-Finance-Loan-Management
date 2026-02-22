package com.hisabkitab.service;

import com.hisabkitab.dto.BorrowerRequest;
import com.hisabkitab.dto.BorrowerResponse;
import com.hisabkitab.entity.Borrower;
import com.hisabkitab.entity.User;
import com.hisabkitab.enums.BorrowerStatus;
import com.hisabkitab.repository.BorrowerRepository;
import com.hisabkitab.repository.LoanRepository;
import com.hisabkitab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * BorrowerService - Business logic for managing borrowers.
 *
 * KEY CONCEPT: Data Isolation
 * Every operation is scoped to the currently logged-in lender.
 * Lender A can NEVER see or modify Lender B's borrowers.
 *
 * We get the current lender from the JWT token via SecurityContext.
 */
@Service
@RequiredArgsConstructor
public class BorrowerService {

    private final BorrowerRepository borrowerRepository;
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final com.hisabkitab.repository.LoanScheduleRepository loanScheduleRepository;

    /**
     * Gets the currently logged-in user from the SecurityContext.
     *
     * When JwtAuthenticationFilter validates the token, it puts the
     * authenticated user into SecurityContextHolder. We retrieve it here.
     *
     * Think of SecurityContextHolder as a "thread-local session" that
     * holds the current user info for the duration of this request.
     */
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new com.hisabkitab.exception.ResourceNotFoundException("User not found!"));
    }

    /**
     * Convert Borrower entity → BorrowerResponse DTO.
     *
     * This is a "mapper" function. In larger projects, you'd use
     * a library like MapStruct for this, but manual mapping is
     * simpler and more educational.
     */
    private BorrowerResponse toResponse(Borrower borrower) {
        return BorrowerResponse.builder()
                .id(borrower.getId())
                .name(borrower.getName())
                .phone(borrower.getPhone())
                .address(borrower.getAddress())
                .notes(borrower.getNotes())
                .status(borrower.getStatus())
                .linkedToUser(borrower.getLinkedUser() != null)
                .createdAt(borrower.getCreatedAt())
                .updatedAt(borrower.getUpdatedAt())
                .build();
    }

    // ==================== CRUD Operations ====================

    /**
     * CREATE - Add a new borrower for the current lender.
     */
    public BorrowerResponse createBorrower(BorrowerRequest request) {
        User lender = getCurrentUser();

        // Check for duplicate borrower name under this lender
        if (borrowerRepository.existsByNameAndLender(request.getName(), lender)) {
            throw new IllegalArgumentException("You already have a borrower named '" + request.getName() + "'!");
        }

        Borrower borrower = Borrower.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .notes(request.getNotes())
                .status(BorrowerStatus.ACTIVE)
                .lender(lender)
                .build();

        Borrower saved = borrowerRepository.save(borrower);
        return toResponse(saved);
    }

    /**
     * READ ALL - Get all borrowers of the current lender.
     */
    public List<BorrowerResponse> getAllBorrowers() {
        User lender = getCurrentUser();
        return borrowerRepository.findByLender(lender)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * READ ALL BY STATUS - Get borrowers filtered by status.
     */
    public List<BorrowerResponse> getBorrowersByStatus(BorrowerStatus status) {
        User lender = getCurrentUser();
        return borrowerRepository.findByLenderAndStatus(lender, status)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * READ ONE - Get a specific borrower by ID.
     * Only returns if the borrower belongs to the current lender.
     */
    public BorrowerResponse getBorrowerById(Long id) {
        User lender = getCurrentUser();
        Borrower borrower = borrowerRepository.findByIdAndLender(id, lender)
                .orElseThrow(() -> new com.hisabkitab.exception.ResourceNotFoundException("Borrower not found with id: " + id));
        return toResponse(borrower);
    }

    /**
     * UPDATE - Update an existing borrower's details.
     */
    public BorrowerResponse updateBorrower(Long id, BorrowerRequest request) {
        User lender = getCurrentUser();
        Borrower borrower = borrowerRepository.findByIdAndLender(id, lender)
                .orElseThrow(() -> new com.hisabkitab.exception.ResourceNotFoundException("Borrower not found with id: " + id));

        // Check for duplicate name if the name is being changed
        if (!borrower.getName().equalsIgnoreCase(request.getName()) &&
            borrowerRepository.existsByNameAndLender(request.getName(), lender)) {
            throw new IllegalArgumentException("You already have a borrower named '" + request.getName() + "'!");
        }

        // Update fields
        borrower.setName(request.getName());
        borrower.setPhone(request.getPhone());
        borrower.setAddress(request.getAddress());
        borrower.setNotes(request.getNotes());
        // Note: @UpdateTimestamp auto-updates 'updatedAt'

        Borrower updated = borrowerRepository.save(borrower);
        return toResponse(updated);
    }

    /**
     * TOGGLE STATUS - Activate or deactivate a borrower.
     * Instead of hard-deleting, we just toggle the status.
     */
    public BorrowerResponse toggleBorrowerStatus(Long id) {
        User lender = getCurrentUser();
        Borrower borrower = borrowerRepository.findByIdAndLender(id, lender)
                .orElseThrow(() -> new com.hisabkitab.exception.ResourceNotFoundException("Borrower not found with id: " + id));

        // Toggle: ACTIVE → INACTIVE, INACTIVE → ACTIVE
        if (borrower.getStatus() == BorrowerStatus.ACTIVE) {
            borrower.setStatus(BorrowerStatus.INACTIVE);
        } else {
            borrower.setStatus(BorrowerStatus.ACTIVE);
        }

        Borrower updated = borrowerRepository.save(borrower);
        return toResponse(updated);
    }

    /**
     * DELETE - Permanently remove a borrower and ALL associated loans + EMIs.
     * 
     * Cascading Delete Order:
     *   1. Delete all loans of this borrower
     *   2. Delete the borrower itself
     */
    @Transactional
    public void deleteBorrower(Long id) {
        User lender = getCurrentUser();
        Borrower borrower = borrowerRepository.findByIdAndLender(id, lender)
                .orElseThrow(() -> new com.hisabkitab.exception.ResourceNotFoundException("Borrower not found with id: " + id));

        // First delete all loans (and their schedules) of this borrower
        var loans = loanRepository.findByBorrowerAndLender(borrower, lender);
        for (com.hisabkitab.entity.Loan loan : loans) {
            loanScheduleRepository.deleteByLoan(loan);
        }
        loanRepository.deleteAll(loans);

        // Finally delete the borrower
        borrowerRepository.delete(borrower);
    }

    /**
     * COUNT - Get total number of borrowers for the current lender.
     */
    public long getBorrowerCount() {
        User lender = getCurrentUser();
        return borrowerRepository.countByLender(lender);
    }

    /**
     * LINK - Link a borrower to a User account.
     *
     * When a borrower registers on the platform, the lender can
     * link their Borrower record to their User account. This enables
     * the borrower to log in and access the Borrower Portal.
     *
     * @param borrowerId The borrower record to link
     * @param userEmail  The email of the User account to link to
     */
    public BorrowerResponse linkBorrowerToUser(Long borrowerId, String userEmail) {
        User lender = getCurrentUser();
        Borrower borrower = borrowerRepository.findByIdAndLender(borrowerId, lender)
                .orElseThrow(() -> new com.hisabkitab.exception.ResourceNotFoundException("Borrower not found with id: " + borrowerId));

        User borrowerUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new com.hisabkitab.exception.ResourceNotFoundException("User not found with email: " + userEmail));

        // Verify the user has BORROWER role
        if (borrowerUser.getRole() != com.hisabkitab.enums.Role.BORROWER) {
            throw new IllegalArgumentException("User must have BORROWER role to be linked!");
        }

        // Check if this user is already linked to another borrower
        if (borrowerRepository.findByLinkedUser(borrowerUser).isPresent()) {
            throw new IllegalStateException("This user is already linked to another borrower!");
        }

        borrower.setLinkedUser(borrowerUser);
        Borrower updated = borrowerRepository.save(borrower);
        return toResponse(updated);
    }
}
