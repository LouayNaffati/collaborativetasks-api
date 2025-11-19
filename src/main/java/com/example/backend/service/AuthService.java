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


    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return new AuthResponse(null, null, null, "Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return new AuthResponse(null, null, null, "Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(User.Role.USER);
        user.setCreatedAt(new Date());

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(token, user.getUsername(), user.getRole().name(), "User registered successfully");
    }


    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            User user = userRepository.findByUsername(request.getUsername()).orElseThrow();
            String token = jwtUtil.generateToken(request.getUsername());
            return new AuthResponse(token, request.getUsername(), user.getRole().name(), "Login successful");

        } catch (Exception e) {
            return new AuthResponse(null, null, null, "Invalid username or password");
        }
    }


    public String forgotPassword(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return "If the email exists, a reset link has been sent";
        }

        User user = userOptional.get();

        String resetToken = UUID.randomUUID().toString();
        user.setResetPasswordToken(resetToken);
        user.setResetPasswordExpires(new Date(System.currentTimeMillis() + 3600000)); // 1h

        userRepository.save(user);

        System.out.println("Reset token for " + email + ": " + resetToken);
        System.out.println("Reset URL: http://your-frontend-url/reset-password?token=" + resetToken);

        return "If the email exists, a reset link has been sent";
    }


    public String resetPassword(String token, String newPassword) {
        Optional<User> userOptional = userRepository.findByResetPasswordToken(token);

        if (userOptional.isEmpty()) {
            return "Invalid reset token";
        }

        User user = userOptional.get();

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
