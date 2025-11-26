package com.example.backend.service;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

   
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

 
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

  
    public long countByRole(User.Role role) {
        return userRepository.countByRole(role);
    }
}
