package com.Jyotibroto.auradrive.dto;

import lombok.Data;

@Data
public class LocationUpdateDto {
    private String driverId;
    private double longitude;
    private double latitude;
}
