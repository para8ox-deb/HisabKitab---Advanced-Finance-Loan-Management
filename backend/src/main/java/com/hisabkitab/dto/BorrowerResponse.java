package com.hisabkitab.dto;

import com.hisabkitab.enums.BorrowerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for returning Borrower data to the client.
 *
 * Excludes sensitive internal data like the full lender object.
 * Only includes what the frontend needs to display.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowerResponse {
    private Long id;
    private String name;
    private String phone;
    private String address;
    private String notes;
    private BorrowerStatus status;
    private boolean linkedToUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
