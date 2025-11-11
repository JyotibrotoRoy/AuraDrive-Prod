package com.Jyotibroto.auradrive.controller;

import com.Jyotibroto.auradrive.dto.LocationDto;
import com.Jyotibroto.auradrive.dto.LocationUpdateDto;
import com.Jyotibroto.auradrive.service.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class LocationController {
    private final DriverService driverService;

    @Autowired
    public LocationController(DriverService driverService) {
        this.driverService = driverService;
    }

    @MessageMapping("/trip/{tripId}/location")
    public void updateLocation(@DestinationVariable String tripId,
                               LocationUpdateDto locationUpdate) {
        driverService.updateAndBroadcastLocation(tripId, locationUpdate);
    }
}
