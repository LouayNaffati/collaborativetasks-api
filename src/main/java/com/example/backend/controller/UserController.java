package com.example.backend.controller;

import com.example.backend.dto.UpdateUserDto;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody UpdateUserDto dto, Authentication authentication) {
        String currentUsername = authentication.getName();
        User user = userRepository.findByUsername(currentUsername).get();

        if (dto.getUsername() != null && !dto.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(dto.getUsername())) {
                return ResponseEntity.badRequest().body("Username already exists");
            }
            user.setUsername(dto.getUsername());
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

        userRepository.save(user);
        return ResponseEntity.ok("User updated successfully");
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
}
