package com.Jyotibroto.auradrive.service;

import com.Jyotibroto.auradrive.dto.UserResponseDTO;
import com.Jyotibroto.auradrive.entity.User;
import com.Jyotibroto.auradrive.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public UserResponseDTO getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserResponseDTO(user);
    }

    private UserResponseDTO mapToUserResponseDTO(User user) {
        UserResponseDTO response = new UserResponseDTO();
        response.setId(user.getId().toString());
        response.setUserName(user.getUserName());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setRole(user.getRole().name());
        response.setAccountStatus(user.getAccountStatus().name()); //
        response.setDocuments(user.getDocuments());
        response.setCurrentVehicleType(user.getCurrentVehicleType());
        return response;
    }
}
