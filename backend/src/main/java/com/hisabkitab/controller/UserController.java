package com.hisabkitab.controller;

import com.hisabkitab.dto.UserProfileResponse;
import com.hisabkitab.dto.UserProfileUpdateRequest;
import com.hisabkitab.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * UserController — REST API for managing logged-in user details.
 * 
 * Endpoints:
 *   GET /api/users/profile    → Fetch current user Profile
 *   PUT /api/users/profile    → Update current user Profile
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(@jakarta.validation.Valid @RequestBody UserProfileUpdateRequest request) {
        userService.updateMyProfile(request);
        return ResponseEntity.ok().build();
    }
}
