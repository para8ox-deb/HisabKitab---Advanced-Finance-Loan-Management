package com.hisabkitab.service;

import com.hisabkitab.dto.UserProfileResponse;
import com.hisabkitab.dto.UserProfileUpdateRequest;
import com.hisabkitab.entity.User;
import com.hisabkitab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get the currently logged-in User from the JWT token.
     */
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new com.hisabkitab.exception.ResourceNotFoundException("User not found!"));
    }

    /**
     * Get the current user's profile details.
     */
    public UserProfileResponse getMyProfile() {
        User user = getCurrentUser();
        return UserProfileResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    /**
     * Update the current user's profile details.
     */
    public void updateMyProfile(UserProfileUpdateRequest request) {
        User user = getCurrentUser();

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }
        
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            // Check if the new email is already taken by someone else
            if (!user.getEmail().equalsIgnoreCase(request.getEmail().trim())) {
                boolean emailExists = userRepository.findByEmail(request.getEmail().trim()).isPresent();
                if (emailExists) {
                    throw new IllegalArgumentException("Email is already in use by another account.");
                }
                user.setEmail(request.getEmail().trim());
            }
        }

        if (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword().trim()));
        }

        userRepository.save(user);
    }
}
