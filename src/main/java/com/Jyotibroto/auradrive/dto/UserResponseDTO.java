package com.Jyotibroto.auradrive.dto;

import com.Jyotibroto.auradrive.POJO.DocumentInfo;
import com.Jyotibroto.auradrive.enums.VehicleType;
import lombok.Data;

import java.util.List;

@Data
public class UserResponseDTO {
    private String id;
    private String userName;
    private String email;
    private String phoneNumber;
    private String role;
    private String accountStatus;
    private List<DocumentInfo> documents;
    private VehicleType currentVehicleType;
}
