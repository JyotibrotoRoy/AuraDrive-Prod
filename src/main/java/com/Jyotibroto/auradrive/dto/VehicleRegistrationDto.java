package com.Jyotibroto.auradrive.dto;

import com.Jyotibroto.auradrive.enums.VehicleType;
import lombok.Data;

@Data
public class VehicleRegistrationDto {
    private String vehicleNumber;
    private String vehicleModel;
    private String vehicleColor;
    private VehicleType vehicleType;
    private String make;
}
