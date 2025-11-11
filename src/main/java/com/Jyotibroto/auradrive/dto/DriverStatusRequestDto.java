package com.Jyotibroto.auradrive.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverStatusRequestDto {
    private boolean available;
    private String vehicleId;
}
