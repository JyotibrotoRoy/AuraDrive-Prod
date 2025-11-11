package com.Jyotibroto.auradrive.controller;

import com.Jyotibroto.auradrive.dto.AuthRequest;
import com.Jyotibroto.auradrive.dto.AuthResponse;
import com.Jyotibroto.auradrive.dto.DriverRegistrationRequestDto;
import com.Jyotibroto.auradrive.dto.RiderRegistrationDto;
import com.Jyotibroto.auradrive.entity.User;
import com.Jyotibroto.auradrive.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register/rider")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RiderRegistrationDto request) {
        authService.registerNewUser(request);
        return ResponseEntity.ok("User Registered succesfully");
    }

    @PostMapping("/register/driver")
    public ResponseEntity<?> registerDriver(@RequestBody DriverRegistrationRequestDto request) {
        authService.registerDriver(request);
        return ResponseEntity.ok("Driver Registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@RequestBody AuthRequest authRequest) {
        String token = authService.login(authRequest);
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
