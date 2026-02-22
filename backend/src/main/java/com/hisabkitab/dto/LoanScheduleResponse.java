package com.hisabkitab.dto;

import com.hisabkitab.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanScheduleResponse {
    private Long id;
    private Long loanId;
    private Integer monthNumber;
    private LocalDate dueDate;
    private BigDecimal amountDue;
    private PaymentStatus status;
    private LocalDate paidDate;
    private String note;
}
