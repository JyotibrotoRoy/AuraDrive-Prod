package com.Jyotibroto.auradrive.dto;

import com.Jyotibroto.auradrive.enums.VehicleType;
import lombok.Data;

@Data
public class FareEstimateResponseDto {
    private VehicleType vehicleType;
    private double estimatedFare;
    private double estimatedDistance; // in kilometers
    private double estimatedDuration; // in minutes
}
