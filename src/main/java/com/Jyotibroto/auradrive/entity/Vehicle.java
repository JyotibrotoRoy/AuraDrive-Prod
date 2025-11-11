package com.Jyotibroto.auradrive.entity;

import com.Jyotibroto.auradrive.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "vehicles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {
    @Id
    private ObjectId id;
    private ObjectId driverId;
    private VehicleType type;
    private String make;
    private String vehicleNumber;
    private String vehicleModel;
    private String vehicleColor;
    private boolean isActive = true;
}
