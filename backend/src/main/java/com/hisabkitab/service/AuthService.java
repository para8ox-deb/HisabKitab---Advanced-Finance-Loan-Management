package com.hisabkitab.service;

import com.hisabkitab.dto.AuthResponse;
import com.hisabkitab.dto.LoginRequest;
import com.hisabkitab.dto.RegisterRequest;
import com.hisabkitab.entity.User;
import com.hisabkitab.enums.Role;
import com.hisabkitab.repository.UserRepository;
import com.hisabkitab.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * AuthService - Handles registration and login business logic.
 *
 * NOTE: UserDetailsService is implemented separately in CustomUserDetailsService
 * to avoid circular dependency with AuthenticationManager.
 *
 * @Service tells Spring: "This is a service class, manage it as a Bean."
 * @RequiredArgsConstructor (Lombok) generates a constructor with all 'final' fields.
 * Spring uses this constructor to inject the dependencies automatically.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    /**
     * Register a new LENDER.
     *
     * Steps:
     * 1. Check if email already exists
     * 2. Create User entity with encoded password
     * 3. Save to database
     * 4. Generate JWT token
     * 5. Return token + user info
     */
    public AuthResponse register(RegisterRequest request) {
        // Check if email is already taken
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered!");
        }

        // Build the User entity
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // Hash the password!
                .role(request.getRole() != null ? request.getRole() : Role.LENDER)
                .active(true)
                .build();

        // Save to database (JPA generates INSERT SQL)
        userRepository.save(user);

        // Generate JWT token for the new user
        String token = jwtUtil.generateToken(user);

        // Return response with token and user info
        return AuthResponse.builder()
                .token(token)
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .message("Registration successful!")
                .build();
    }

    /**
     * Login an existing user.
     *
     * Steps:
     * 1. Authenticate with Spring Security (checks email + password)
     * 2. Load user from database
     * 3. Generate JWT token
     * 4. Return token + user info
     */
    public AuthResponse login(LoginRequest request) {
        try {
            // This triggers Spring Security's authentication process:
            // 1. Loads user via CustomUserDetailsService.loadUserByUsername()
            // 2. Compares passwords using BCrypt
            // 3. Throws BadCredentialsException if wrong
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new IllegalArgumentException("Invalid email or password!");
        }

        // If authentication passed, load the user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new com.hisabkitab.exception.ResourceNotFoundException("User not found!"));

        // Check if user is active
        if (!user.isActive()) {
            throw new IllegalStateException("Account is deactivated. Contact admin.");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .message("Login successful!")
                .build();
    }
}
