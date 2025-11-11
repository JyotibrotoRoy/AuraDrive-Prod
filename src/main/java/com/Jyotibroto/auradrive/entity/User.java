package com.Jyotibroto.auradrive.entity;

import com.Jyotibroto.auradrive.POJO.DocumentInfo;
import com.Jyotibroto.auradrive.enums.AccountStatus;
import com.Jyotibroto.auradrive.enums.ROLES;
import com.Jyotibroto.auradrive.enums.VehicleType;
import com.mongodb.lang.NonNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private ObjectId id;

    @Indexed(unique = true)
    @NonNull
    private String userName;
    @Indexed(unique = true)
    private String email;
    @Indexed(unique = true)
    private String phoneNumber;
    @NonNull
    private String password;
    private ROLES role;
    private boolean available = false;
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint currentLocation;

    private List<String> vehicleIds = new ArrayList<>();
    private ObjectId currentVehicleId;
    private VehicleType currentVehicleType;

    private AccountStatus accountStatus;
    private List<DocumentInfo> documents = new ArrayList<>();
}
