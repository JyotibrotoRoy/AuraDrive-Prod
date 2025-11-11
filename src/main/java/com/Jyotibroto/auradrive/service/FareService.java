package com.Jyotibroto.auradrive.service;

import com.Jyotibroto.auradrive.dto.FareEstimateResponseDto;
import com.Jyotibroto.auradrive.dto.LocationDto;
import com.Jyotibroto.auradrive.entity.Trip;
import com.Jyotibroto.auradrive.enums.VehicleType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FareService {
    private record EstimatedRoute(double distanceKm, long durationMinutes) {}
    private EstimatedRoute getEstimatedRoute(LocationDto start, LocationDto end) {
        // In a real app, we will call Google Maps API here
        double exampleDistance = 10.5; // kilometers
        long exampleDuration = 25;   // minutes
        return new EstimatedRoute(exampleDistance, exampleDuration); // Example: 10.5 km, 25 minutes
    }

    public List<FareEstimateResponseDto> calculateEstimates(LocationDto start, LocationDto end) {
        EstimatedRoute route = getEstimatedRoute(start, end);
        List<FareEstimateResponseDto> estimates = new ArrayList<>();

        for(VehicleType type : VehicleType.values()) {
            double baseFare;
            double perKmRate;
            double perMinuteRate;
            switch (type) {
                case BIKE: baseFare = 25.0; perKmRate = 8.0; perMinuteRate = 1.0; break;
                case AUTO: baseFare = 40.0; perKmRate = 12.0; perMinuteRate = 1.5; break;
                case SEDAN: baseFare = 60.0; perKmRate = 15.0; perMinuteRate = 2.0; break;
                case SUV: baseFare = 80.0; perKmRate = 18.0; perMinuteRate = 2.5; break;
                default: continue; // Skip if type not supported
            }
            double fare = baseFare + (route.distanceKm() * perKmRate) + (route.durationMinutes() * perMinuteRate);
            FareEstimateResponseDto dto = new FareEstimateResponseDto();
            dto.setVehicleType(type);
            dto.setEstimatedFare(Math.round(fare * 100.0) / 100.0); // Round to 2 decimals
            dto.setEstimatedDistance(route.distanceKm());
            dto.setEstimatedDuration(route.durationMinutes());
            estimates.add(dto);
        }
        return  estimates;
    }
    public double calculateFare(Trip trip) {
        VehicleType type = trip.getVehicleType();

        LocationDto startLocation = new LocationDto();
        startLocation.setLongitude(trip.getStartLocation().getX());
        startLocation.setLatitude(trip.getStartLocation().getY());

        LocationDto endLocation = new LocationDto();
        endLocation.setLongitude(trip.getEndLocation().getX());
        endLocation.setLatitude(trip.getEndLocation().getY());

        EstimatedRoute route = getEstimatedRoute(startLocation, endLocation);

        long durationMinutes;
        double distanceKm;
        if(trip.getStartedAt() != null && trip.getEndedAt() != null) {
            durationMinutes = java.time.Duration.between(
                        trip.getStartedAt(),
                        trip.getEndedAt()).toMinutes();

            distanceKm = route.distanceKm();
        }else {
            distanceKm = route.distanceKm();
            durationMinutes = route.durationMinutes();
        }

        double baseFare;
        double perKmRate;
        double perMinuteRate;
        switch (type) {
            case BIKE: baseFare = 25.0; perKmRate = 8.0; perMinuteRate = 1.0; break;
            case AUTO: baseFare = 40.0; perKmRate = 12.0; perMinuteRate = 1.5; break;
            case SEDAN: baseFare = 60.0; perKmRate = 15.0; perMinuteRate = 2.0; break;
            case SUV: baseFare = 80.0; perKmRate = 18.0; perMinuteRate = 2.5; break;
            default: throw new IllegalArgumentException("Unsupported vehicle type for trip: " + type);
        }
        double finalFare = baseFare + (distanceKm * perKmRate) + (durationMinutes * perMinuteRate);
        return Math.round(finalFare * 100.0) / 100.0;
    }
}

