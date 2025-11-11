package com.Jyotibroto.auradrive.dto;

import com.Jyotibroto.auradrive.enums.TripStatus;
import com.Jyotibroto.auradrive.enums.VehicleType;
import lombok.Data;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

@Data
public class TripResponseDto {
    private String id;
    private String riderId;
    private String diverId;
    private LocationDto startLocation;
    private LocationDto endLocation;
    private TripStatus status;
    private LocalDateTime createdAt;
    private String otpForRider;
    private double finalFare;
    private VehicleType vehicleType;
    private String vehicleNumber;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
}
