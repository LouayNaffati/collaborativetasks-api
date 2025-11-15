package com.example.backend.service;

import com.example.backend.dto.AuthResponse;
import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.dto.UserDto;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;


    // ===================== GET USERS BY ROLE =====================
    public List<UserDto> getUsersByRole(User.Role role) {
        List<User> users = userRepository.findByRole(role);
        return users.stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    private UserDto convertToUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
        );
    }


    // ===================== REGISTER =====================
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return new AuthResponse(null, null, "Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return new AuthResponse(null, null, "Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(User.Role.USER);

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(token, user.getUsername(), "User registered successfully");
    }


    // ===================== LOGIN =====================
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            String token = jwtUtil.generateToken(request.getUsername());
            return new AuthResponse(token, request.getUsername(), "Login successful");

        } catch (Exception e) {
            return new AuthResponse(null, null, "Invalid username or password");
        }
    }


    // ===================== FORGOT PASSWORD =====================
    public String forgotPassword(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        // Always return a generic message (security best practice)
        if (userOptional.isEmpty()) {
            return "If the email exists, a reset link has been sent";
        }

        User user = userOptional.get();

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        user.setResetPasswordToken(resetToken);
        user.setResetPasswordExpires(new Date(System.currentTimeMillis() + 3600000)); // 1h

        userRepository.save(user);

        // TODO: replace with real email sending service
        System.out.println("Reset token for " + email + ": " + resetToken);
        System.out.println("Reset URL: http://your-frontend-url/reset-password?token=" + resetToken);

        return "If the email exists, a reset link has been sent";
    }


    // ===================== RESET PASSWORD =====================
    public String resetPassword(String token, String newPassword) {
        Optional<User> userOptional = userRepository.findByResetPasswordToken(token);

        if (userOptional.isEmpty()) {
            return "Invalid reset token";
        }

        User user = userOptional.get();

        // Token expired
        if (user.getResetPasswordExpires().before(new Date())) {
            return "Reset token has expired";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordExpires(null);

        userRepository.save(user);

        return "Password reset successfully";
    }
}
