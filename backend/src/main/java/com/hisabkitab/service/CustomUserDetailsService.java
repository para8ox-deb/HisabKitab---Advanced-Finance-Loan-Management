package com.hisabkitab.service;

import com.hisabkitab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * CustomUserDetailsService - Separated from AuthService to avoid circular dependency.
 *
 * WHY is this separate?
 * AuthService needs AuthenticationManager → which needs UserDetailsService
 * If AuthService IS the UserDetailsService AND depends on AuthenticationManager,
 * Spring can't create either one first → circular dependency!
 *
 * Solution: Separate the UserDetailsService into its own class.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Spring Security calls this method during authentication.
     * It loads the user from the database by email.
     *
     * Our User entity implements UserDetails, so we can return it directly.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
