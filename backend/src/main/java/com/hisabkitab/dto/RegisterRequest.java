package com.hisabkitab.dto;

import com.hisabkitab.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) for registration requests.
 *
 * WHY DTOs?
 * - We don't expose our Entity directly to the outside world (security risk!)
 * - Entity has fields like "id", "createdAt" which the client shouldn't send
 * - DTOs let us control exactly what data comes IN and goes OUT
 *
 * Think of it like a form: this class defines what fields the registration form has.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters")
    private String password;

    /**
     * Optional role field. Defaults to LENDER if not specified.
     * Allows BORROWER registration for the Borrower Portal.
     */
    private Role role = Role.LENDER;
}
