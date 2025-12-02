package com.example.backend.controller;

import com.example.backend.dto.UpdateUserDto;
import com.example.backend.model.User;
import com.example.backend.util.JwtUtil;
import com.example.backend.dto.AuthResponse;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody UpdateUserDto dto, Authentication authentication) {
        String currentUsername = authentication.getName();
        User user = userRepository.findByUsername(currentUsername).orElse(null);

        if (user == null) {
            return ResponseEntity.status(401).body("User not found or not authenticated");
        }

        String oldUsername = user.getUsername();
        boolean usernameChanged = false;

        if (dto.getUsername() != null && !dto.getUsername().equals(oldUsername)) {
            if (userRepository.existsByUsername(dto.getUsername())) {
                return ResponseEntity.badRequest().body("Username already exists");
            }
            user.setUsername(dto.getUsername());
            usernameChanged = true;
        }

        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                return ResponseEntity.badRequest().body("Email already exists");
            }
            user.setEmail(dto.getEmail());
        }

        if (dto.getProfileImage() != null) {
            user.setProfileImage(dto.getProfileImage());
        }

    User saved = userRepository.save(user);

    // If username changed, generate a new JWT and return it along with user info so client can replace token
    if (usernameChanged) {
        String token = jwtUtil.generateToken(saved.getUsername());
        AuthResponse authResponse = new AuthResponse(token, saved.getUsername(), saved.getRole() != null ? saved.getRole().name() : null, "Username changed. Use the returned token for subsequent requests.");
        return ResponseEntity.ok(authResponse);
    }

    com.example.backend.dto.UserDto response = new com.example.backend.dto.UserDto(
        saved.getId(),
        saved.getUsername(),
        saved.getEmail(),
        saved.getRole() != null ? saved.getRole().name() : null,
        saved.getCreatedAt() != null ? saved.getCreatedAt().toString() : null
    );

    return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).get();
        UpdateUserDto dto = new UpdateUserDto();
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setProfileImage(user.getProfileImage());
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteProfile(Authentication authentication) {
        String currentUsername = authentication.getName();
        User user = userRepository.findByUsername(currentUsername).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found or not authenticated");
        }

        try {
            userRepository.delete(user);
            return ResponseEntity.ok("Profile deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete profile: " + e.getMessage());
        }
    }
}
