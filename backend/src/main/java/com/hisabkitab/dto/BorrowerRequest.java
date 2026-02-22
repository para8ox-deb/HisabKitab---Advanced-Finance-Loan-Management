package com.hisabkitab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating/updating a Borrower.
 *
 * Only includes fields the client should send.
 * lender_id is NOT here — it comes from the JWT token (the logged-in user).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowerRequest {

    @NotBlank(message = "Borrower name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(max = 15, message = "Phone number must be at most 15 characters")
    private String phone;

    @Size(max = 255, message = "Address must be at most 255 characters")
    private String address;

    @Size(max = 500, message = "Notes must be at most 500 characters")
    private String notes;
}
