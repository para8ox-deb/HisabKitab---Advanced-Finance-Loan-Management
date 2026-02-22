package com.hisabkitab.repository;

import com.hisabkitab.entity.Borrower;
import com.hisabkitab.entity.Loan;
import com.hisabkitab.entity.User;
import com.hisabkitab.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * LoanRepository - Database operations for "loans" table.
 * All queries are scoped by lender for data isolation.
 */
@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    /** All loans for a lender. */
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"borrower"})
    List<Loan> findByLender(User lender);

    /** All loans for a lender, filtered by status. */
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"borrower"})
    List<Loan> findByLenderAndStatus(User lender, LoanStatus status);

    /** All loans for a specific borrower of a lender. */
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"borrower"})
    List<Loan> findByBorrowerAndLender(Borrower borrower, User lender);

    /** Find a specific loan, only if it belongs to this lender. */
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"borrower"})
    Optional<Loan> findByIdAndLender(Long id, User lender);

    /** Count loans by lender. */
    long countByLender(User lender);

    /** Count active loans by lender. */
    long countByLenderAndStatus(User lender, LoanStatus status);

    /** All loans for a specific borrower (across all lenders). Used by Borrower Portal. */
    List<Loan> findByBorrower(Borrower borrower);
    
    /** Delete all loans for a lender */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByLender(User lender);
    
    /** Delete all loans for a specific borrower */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByBorrower(Borrower borrower);
}
