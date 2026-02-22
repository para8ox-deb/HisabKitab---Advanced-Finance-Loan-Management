package com.hisabkitab.config;

import com.hisabkitab.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * SecurityConfig - The brain of Spring Security configuration.
 *
 * This class controls:
 * 1. Which endpoints are public vs protected
 * 2. How passwords are encoded
 * 3. How authentication works
 * 4. CORS settings (for React frontend)
 * 5. Session management (STATELESS for JWT)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    /**
     * SecurityFilterChain - Defines the security rules for HTTP requests.
     *
     * Think of this as a bouncer at a club:
     * - Some doors are open to everyone (public endpoints)
     * - Some doors require a VIP pass (authenticated endpoints)
     * - Some doors are role-restricted (ADMIN only, LENDER only)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (Cross-Site Request Forgery) protection
            // because we're using JWT tokens (stateless) instead of cookies
            .csrf(csrf -> csrf.disable())

            // Enable CORS so React frontend can talk to this backend
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Define which endpoints are public and which need authentication
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - anyone can access
                .requestMatchers("/api/auth/**").permitAll()

                // Admin-only endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // Lender endpoints explicitly secured
                .requestMatchers("/api/loans/**", "/api/borrowers/**", "/api/dashboard/**", "/api/reports/**", "/api/users/**").hasRole("LENDER")

                // Borrower endpoints (their own portal)
                .requestMatchers("/api/borrower-portal/**").hasRole("BORROWER")

                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            // STATELESS session management
            // We don't store session on server (no cookies)
            // Every request must carry its own JWT token
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Use our custom authentication provider
            .authenticationProvider(authenticationProvider())

            // Add our JWT filter BEFORE Spring's default auth filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS Configuration - Allows React frontend to call our API.
     *
     * Without CORS, the browser would block requests from
     * http://localhost:5173 (React) to http://localhost:8080 (Spring Boot)
     * because they are on different ports (Cross-Origin).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allowed origins can be overridden in production via environment variable
        String allowedOrigins = System.getenv("ALLOWED_ORIGINS");
        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        } else {
            configuration.setAllowedOrigins(List.of("http://localhost:5173")); // Vite default port
        }
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * PasswordEncoder - Uses BCrypt to hash passwords.
     *
     * BCrypt is a one-way hashing algorithm:
     *   "password123" → "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
     *
     * Even if the database is hacked, passwords can't be reversed.
     * BCrypt also adds a random "salt" so same password → different hash each time.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationProvider - Tells Spring HOW to authenticate users.
     *
     * DaoAuthenticationProvider:
     * 1. Uses UserDetailsService to load user from database
     * 2. Uses PasswordEncoder to compare passwords
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * AuthenticationManager - The entry point for authentication.
     * We need this bean to call authenticate() in our AuthService.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
