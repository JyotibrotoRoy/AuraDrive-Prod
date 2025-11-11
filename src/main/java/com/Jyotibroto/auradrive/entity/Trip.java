package com.Jyotibroto.auradrive.entity;

import com.Jyotibroto.auradrive.enums.TripStatus;
import com.Jyotibroto.auradrive.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import java.rmi.server.ObjID;
import java.time.LocalDateTime;

@Document(collection = "trips")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Trip {
    @Id
    private String id;
    private ObjectId riderId;
    private ObjectId driverId;
    private GeoJsonPoint startLocation;
    private GeoJsonPoint endLocation;
    private TripStatus tripStatus;
    private String Otp;
    private LocalDateTime createdAt;

    private VehicleType vehicleType;
    private String vehicleNumber;
    private double finalFare;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
}
