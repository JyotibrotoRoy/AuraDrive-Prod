package com.Jyotibroto.auradrive.dto;


import lombok.Data;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

@Data
public class LocationDto {
    private double longitude;
    private double latitude;
}
