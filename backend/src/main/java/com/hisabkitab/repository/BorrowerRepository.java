package com.hisabkitab.repository;

import com.hisabkitab.entity.Borrower;
import com.hisabkitab.entity.User;
import com.hisabkitab.enums.BorrowerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * BorrowerRepository - Database operations for the "borrowers" table.
 *
 * All queries include the lender (User) parameter to ensure
 * DATA ISOLATION — a lender can only access their own borrowers.
 */
@Repository
public interface BorrowerRepository extends JpaRepository<Borrower, Long> {

    /**
     * Find all borrowers belonging to a specific lender.
     * SQL: SELECT * FROM borrowers WHERE lender_id = ?
     */
    List<Borrower> findByLender(User lender);

    /**
     * Find all borrowers of a lender filtered by status.
     * SQL: SELECT * FROM borrowers WHERE lender_id = ? AND status = ?
     */
    List<Borrower> findByLenderAndStatus(User lender, BorrowerStatus status);

    /**
     * Find a specific borrower by ID, but only if they belong to this lender.
     * SQL: SELECT * FROM borrowers WHERE id = ? AND lender_id = ?
     *
     * This prevents Lender A from accessing Lender B's borrowers.
     */
    Optional<Borrower> findByIdAndLender(Long id, User lender);

    /**
     * Check if a borrower with this name already exists for this lender.
     * SQL: SELECT COUNT(*) > 0 FROM borrowers WHERE name = ? AND lender_id = ?
     */
    boolean existsByNameAndLender(String name, User lender);

    /**
     * Count total borrowers for a lender.
     * SQL: SELECT COUNT(*) FROM borrowers WHERE lender_id = ?
     */
    long countByLender(User lender);

    /**
     * Find the borrower linked to a specific User account.
     * Used by the Borrower Portal to identify which borrower is logged in.
     * SQL: SELECT * FROM borrowers WHERE linked_user_id = ?
     */
    Optional<Borrower> findByLinkedUser(User linkedUser);
    
    /** Delete all borrowers for a lender */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByLender(User lender);
}
