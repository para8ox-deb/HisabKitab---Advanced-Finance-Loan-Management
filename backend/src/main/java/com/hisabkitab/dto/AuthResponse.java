package com.hisabkitab.dto;

import com.hisabkitab.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for authentication responses.
 *
 * After successful login/register, we send back:
 * - JWT token (for subsequent API calls)
 * - Basic user info (so the frontend knows who logged in)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String name;
    private String email;
    private Role role;
    private String message;
}
