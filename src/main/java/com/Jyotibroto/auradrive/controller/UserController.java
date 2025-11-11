package com.Jyotibroto.auradrive.controller;

import com.Jyotibroto.auradrive.dto.TripResponseDto;
import com.Jyotibroto.auradrive.dto.UserResponseDTO;
import com.Jyotibroto.auradrive.repository.TripRepository;
import com.Jyotibroto.auradrive.service.TripService;
import com.Jyotibroto.auradrive.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final TripRepository tripRepository;
    private final TripService tripService;

    public UserController(UserService userService, TripRepository tripRepository, TripService tripService) {
        this.tripRepository = tripRepository;
        this.userService = userService;
        this.tripService = tripService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponseDTO userProfile = userService.getUserProfile(userDetails.getUsername());
        return ResponseEntity.ok(userProfile);
    }

    @GetMapping("/me/trips")
    public ResponseEntity<List<TripResponseDto>> getMyTrips(@AuthenticationPrincipal UserDetails userDetails) {
        List<TripResponseDto> trips = tripService.getTripHistory(userDetails);
        return ResponseEntity.ok(trips);
    }
}
