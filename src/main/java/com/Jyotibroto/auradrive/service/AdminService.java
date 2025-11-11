package com.Jyotibroto.auradrive.service;

import com.Jyotibroto.auradrive.dto.UserResponseDTO;
import com.Jyotibroto.auradrive.entity.User;
import com.Jyotibroto.auradrive.enums.AccountStatus;
import com.Jyotibroto.auradrive.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {
    private final UserRepository userRepository;
    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getPendingDriverApprovals(){
        return userRepository.findByAccountStatus(AccountStatus.PENDING_APPROVAL);
    }

    public void approveDriverAccount(String driverId){
        User driver = userRepository.findById(new ObjectId(driverId))
                .orElseThrow(() -> new RuntimeException("Driver not found with ID: " + driverId));

        if(driver.getAccountStatus() != AccountStatus.PENDING_APPROVAL){
            throw new IllegalStateException("Driver account is not pending approval.");
        }

        driver.setAccountStatus(AccountStatus.ACCEPTED);
        userRepository.save(driver);
    }

    public void rejectDriverAccount(String driverId){
        User driver = userRepository.findById(new ObjectId(driverId))
                .orElseThrow(() -> new RuntimeException("Driver not found with ID: " + driverId));

        if(driver.getAccountStatus() != AccountStatus.PENDING_APPROVAL){
            throw new IllegalStateException("Driver account is not pending approval.");
        }

        driver.setAccountStatus(AccountStatus.REJECTED);
        userRepository.save(driver);
    }
}
