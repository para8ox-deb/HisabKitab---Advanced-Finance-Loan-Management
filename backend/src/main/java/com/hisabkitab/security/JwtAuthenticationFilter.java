package com.hisabkitab.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter - Intercepts EVERY HTTP request.
 *
 * This filter runs BEFORE the request reaches your controller.
 * It checks if the request has a valid JWT token in the "Authorization" header.
 *
 * Flow:
 * 1. Client sends: Authorization: Bearer eyJhbG...
 * 2. Filter extracts the token (removes "Bearer " prefix)
 * 3. Validates the token
 * 4. If valid → sets the user in SecurityContext (user is "logged in")
 * 5. If invalid or missing → request continues without authentication
 *    (Spring Security will block it if the endpoint requires auth)
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Step 1: Get the Authorization header
        final String authHeader = request.getHeader("Authorization");

        // Step 2: Check if header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No token found → continue to next filter (will be blocked by security if needed)
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3: Extract the JWT token (remove "Bearer " prefix)
        final String jwt = authHeader.substring(7);

        try {
            // Step 4: Extract email from the token
            final String userEmail = jwtUtil.extractEmail(jwt);

            // Step 5: If email exists and user is not already authenticated
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Step 6: Load the user from database
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                // Step 7: Validate the token
                if (jwtUtil.validateToken(jwt, userDetails)) {

                    // Step 8: Create authentication token and set it in SecurityContext
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Step 9: Set the authenticated user in the security context
                    // After this, Spring Security considers the user "logged in" for this request
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token is invalid or expired — just continue without authentication
            // The security config will handle blocking unauthorized requests
        }

        // Step 10: Continue the filter chain
        filterChain.doFilter(request, response);
    }
}
