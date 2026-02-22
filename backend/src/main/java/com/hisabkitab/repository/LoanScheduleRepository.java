package com.hisabkitab.repository;

import com.hisabkitab.entity.Loan;
import com.hisabkitab.entity.LoanSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanScheduleRepository extends JpaRepository<LoanSchedule, Long> {
    List<LoanSchedule> findByLoanOrderByMonthNumberAsc(Loan loan);
    void deleteByLoan(Loan loan);
}
