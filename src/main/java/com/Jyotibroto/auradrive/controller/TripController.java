package com.Jyotibroto.auradrive.controller;

import com.Jyotibroto.auradrive.dto.FareEstimateResponseDto;
import com.Jyotibroto.auradrive.dto.StartTripRequestDto;
import com.Jyotibroto.auradrive.dto.TripRequestDto;
import com.Jyotibroto.auradrive.dto.TripResponseDto;
import com.Jyotibroto.auradrive.enums.TripStatus;
import com.Jyotibroto.auradrive.service.FareService;
import com.Jyotibroto.auradrive.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    @Autowired
    private TripService tripService;
    @Autowired
    private FareService fareService;

    //Create trip
    @PostMapping
    public ResponseEntity<TripResponseDto> createTrip(@RequestBody TripRequestDto request,
                                                      @AuthenticationPrincipal UserDetails riderDetails) {
        TripResponseDto response = tripService.createTrip(request, riderDetails);
        return ResponseEntity.ok(response);
    }

    //Accept trip
    @PatchMapping("/{tripId}/accept")
    public ResponseEntity<TripResponseDto> acceptTrip(@PathVariable String tripId,
                                                      @AuthenticationPrincipal UserDetails driverDetails) {
        TripResponseDto response = tripService.acceptTrip(tripId, driverDetails);
        return ResponseEntity.ok(response);
    }

    //Cancel Trip
    @PatchMapping("/{tripId}/cancel")
    public ResponseEntity<TripResponseDto> cancelTrip(@PathVariable String tripId,
                                                      @AuthenticationPrincipal UserDetails driverDetails) {
        TripResponseDto response = tripService.cancelTrip(tripId, driverDetails);
        return ResponseEntity.ok(response);
    }

    //Start Trip
    @PatchMapping("/{tripId}/start")
    public ResponseEntity<TripResponseDto> startTrip(@PathVariable String tripId,
                                                     @RequestBody StartTripRequestDto request,
                                                     @AuthenticationPrincipal UserDetails driverDetails) {
        TripResponseDto response = tripService.startTrip(tripId, request.getOtp(), driverDetails);
        return ResponseEntity.ok(response);
    }

    //End Trip
    @PatchMapping("/{tripId}/end")
    public ResponseEntity<TripResponseDto> endTrip(@PathVariable String tripId,
                                                   @AuthenticationPrincipal UserDetails driverDetails) {
        TripResponseDto response = tripService.endTrip(tripId, driverDetails);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/estimate" )
    public ResponseEntity<List<FareEstimateResponseDto>> estimateFare(@RequestBody TripRequestDto request) {
        List<FareEstimateResponseDto> estimates = fareService.calculateEstimates(
                request.getStartLocation(), request.getEndLocation()
        );
        return ResponseEntity.ok(estimates);
    }
}
