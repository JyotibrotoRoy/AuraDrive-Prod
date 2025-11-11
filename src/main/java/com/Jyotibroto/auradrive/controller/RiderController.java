package com.Jyotibroto.auradrive.controller;

import com.Jyotibroto.auradrive.dto.FindDriverRequestDto;
import com.Jyotibroto.auradrive.dto.LocationDto;
import com.Jyotibroto.auradrive.dto.NearbyDriversResponseDto;
import com.Jyotibroto.auradrive.service.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/riders")
public class RiderController {

    @Autowired
    private DriverService driverService;

    @PostMapping("/nearby")
    public ResponseEntity<List<NearbyDriversResponseDto>> findNearByDrivers(@RequestBody FindDriverRequestDto request) {
        List<NearbyDriversResponseDto> drivers = driverService.findNearByDrivers(request.getCurrentLocation(), request.getVehicleType());
        return ResponseEntity.ok(drivers);
    }

}
