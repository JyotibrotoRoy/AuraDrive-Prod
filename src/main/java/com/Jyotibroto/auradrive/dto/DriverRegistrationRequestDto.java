package com.Jyotibroto.auradrive.dto;

import lombok.Data;

@Data
public class DriverRegistrationRequestDto {
    private String userName;
    private String email;
    private String phoneNumber;
    private String password;
}
