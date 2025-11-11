package com.Jyotibroto.auradrive.dto;

import com.Jyotibroto.auradrive.enums.VehicleType;
import lombok.Data;

@Data
public class NearbyDriversResponseDto {
    private String id;
    private String userName;
    private LocationDto currentLocation;
    private VehicleType vehicleType;

}
