package com.Jyotibroto.auradrive.service;

import com.Jyotibroto.auradrive.dto.AuthRequest;
import com.Jyotibroto.auradrive.dto.DriverRegistrationRequestDto;
import com.Jyotibroto.auradrive.dto.RiderRegistrationDto;
import com.Jyotibroto.auradrive.entity.User;
import com.Jyotibroto.auradrive.enums.AccountStatus;
import com.Jyotibroto.auradrive.enums.ROLES;
import com.Jyotibroto.auradrive.repository.UserRepository;
import com.Jyotibroto.auradrive.util.JwtUtil;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerNewUser(RiderRegistrationDto request) {
        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Error: A user with this email already exists");
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setUserName(request.getUserName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(ROLES.RIDER);
        user.setAccountStatus(AccountStatus.ACCEPTED);
        user.setAvailable(true); // Riders are always available
        try {
            User savedUser = userRepository.save(user);
            log.info("Successfully registered new user with email id: {}", savedUser.getEmail());
            return savedUser;
        }catch (DataIntegrityViolationException e) {
            log.error("Database error while registering new user with email id: {}. error: {}", user.getEmail(), e.getMessage());
            throw new IllegalStateException("Error: this phone number or email may already be in use");
        }
    }

    @Transactional
    public User registerDriver(DriverRegistrationRequestDto request) {
        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Error: A user with this email already exists");
        }

        User user = new User();
        user.setUserName(request.getUserName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(ROLES.DRIVER);
        user.setAccountStatus(AccountStatus.PENDING_DOCUMENTS);
        user.setAvailable(false);
        try{
            User savedUser = userRepository.save(user);
            log.info("Successfully registered new driver with email id: {}", savedUser.getEmail());
            return savedUser;
        }catch (DataIntegrityViolationException e){
            log.error("Database error while registering new driver with email id: {}. error: {}", user.getEmail(), e.getMessage());
            throw new IllegalStateException("Error: this phone number or email may already be in use");
        }
    }

    public String login(AuthRequest authRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authRequest.getEmail(),
                authRequest.getPassword()));
        return jwtUtil.generateToken(authRequest.getEmail());
    }
}
