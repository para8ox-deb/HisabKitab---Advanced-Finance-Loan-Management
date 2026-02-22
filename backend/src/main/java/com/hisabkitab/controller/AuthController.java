package com.hisabkitab.controller;

import com.hisabkitab.dto.AuthResponse;
import com.hisabkitab.dto.LoginRequest;
import com.hisabkitab.dto.RegisterRequest;
import com.hisabkitab.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController - REST API endpoints for authentication.
 *
 * @RestController = @Controller + @ResponseBody
 *   - @Controller: Marks this as an HTTP request handler
 *   - @ResponseBody: Automatically converts return objects to JSON
 *
 * @RequestMapping("/api/auth") - Base URL prefix for all endpoints in this controller
 *
 * Available endpoints:
 *   POST /api/auth/register → Register a new lender
 *   POST /api/auth/login    → Login and get JWT token
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/register
     *
     * Request Body (JSON):
     * {
     *   "name": "Aakash",
     *   "email": "aakash@example.com",
     *   "password": "password123"
     * }
     *
     * Response (JSON):
     * {
     *   "token": "eyJhbG...",
     *   "name": "Aakash",
     *   "email": "aakash@example.com",
     *   "role": "LENDER",
     *   "message": "Registration successful!"
     * }
     *
     * @Valid triggers validation on the RegisterRequest fields.
     * If validation fails, Spring automatically returns 400 Bad Request
     * with the error messages we defined in the DTO.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/login
     *
     * Request Body (JSON):
     * {
     *   "email": "aakash@example.com",
     *   "password": "password123"
     * }
     *
     * Response (JSON):
     * {
     *   "token": "eyJhbG...",
     *   "name": "Aakash",
     *   "email": "aakash@example.com",
     *   "role": "LENDER",
     *   "message": "Login successful!"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
