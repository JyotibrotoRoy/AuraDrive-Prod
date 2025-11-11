package com.Jyotibroto.auradrive.dto;

import com.Jyotibroto.auradrive.enums.VehicleType;
import lombok.Data;

@Data
public class TripRequestDto {
    private LocationDto startLocation;
    private LocationDto endLocation;
    private VehicleType vehicleType;
}
