package com.Jyotibroto.auradrive.controller;

import com.Jyotibroto.auradrive.dto.UserResponseDTO;
import com.Jyotibroto.auradrive.entity.User;
import com.Jyotibroto.auradrive.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/drivers/pending-approvals")
    public ResponseEntity<List<UserResponseDTO>> getPendingDrivers(){
        List<User> pendingDrivers = adminService.getPendingDriverApprovals();
        List<UserResponseDTO> response = pendingDrivers.stream()
                .map(this:: mapToUserResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/drivers/{driverId}/approve")
    public ResponseEntity<String> approveDriver(@PathVariable String driverId){
        adminService.approveDriverAccount(driverId);
        return ResponseEntity.ok("Driver Approved Succesfully");
    }

    @PatchMapping("/drivers/{driverId}/reject")
    public ResponseEntity<String> rejectDriver(@PathVariable String driverId){
        adminService.rejectDriverAccount(driverId);
        return ResponseEntity.ok("Driver Rejected Succesfully");
    }

    private UserResponseDTO mapToUserResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId().toString());
        dto.setUserName(user.getUserName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole().toString());
        dto.setAccountStatus(user.getAccountStatus().toString());
        dto.setDocuments(user.getDocuments());
        return dto;
    }
}
