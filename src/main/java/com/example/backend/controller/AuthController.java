package com.example.backend.controller;

import com.example.backend.dto.AuthResponse;
import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.dto.ForgotPasswordRequest;
import com.example.backend.dto.ResetPasswordRequest;
import com.example.backend.dto.UserDto;

import com.example.backend.model.User;
import com.example.backend.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    
    @GetMapping("/users/by-role")
    public ResponseEntity<?> getUsersByRole(@RequestParam(required = false) User.Role role) {
        try {
            // Check if the role parameter is missing or invalid
            if (role == null) {
                return ResponseEntity.badRequest().body("Error: Role parameter is missing or invalid. Please provide a valid role.");
            }

            // Fetch users by role
            List<UserDto> users = authService.getUsersByRole(role);

            // Check if no users are found for the given role
            if (users.isEmpty()) {
                return ResponseEntity.status(404).body("Error: No users found for the specified role: " + role);
            }

            // Return the list of users if everything is fine
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            // Handle specific illegal argument exceptions
            return ResponseEntity.badRequest().body("Error: Invalid role provided. Details: " + e.getMessage());
        } catch (NullPointerException e) {
            // Handle null pointer exceptions
            return ResponseEntity.status(500).body("Error: A required value was null. Details: " + e.getMessage());
        } catch (Exception e) {
            // Handle any other unexpected exceptions
            return ResponseEntity.status(500).body("Error: An unexpected error occurred. Details: " + e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        String response = authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        String response = authService.resetPassword(request.getToken(), request.getNewPassword());

        if (response.equals("Password reset successfully")) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);

        if (response.getToken() != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);

        if (response.getToken() != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }
}
