package com.Jyotibroto.auradrive.controller;

import com.Jyotibroto.auradrive.dto.*;
import com.Jyotibroto.auradrive.entity.Vehicle;
import com.Jyotibroto.auradrive.enums.DocumentType;
import com.Jyotibroto.auradrive.service.DriverService;
import com.Jyotibroto.auradrive.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/drivers")
public class DriverController {
    @Autowired
    private DriverService driverService;

    private final FileStorageService fileStorageService;

    public DriverController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PatchMapping("/me/status")
    public ResponseEntity<?> updateAvailability(@RequestBody DriverStatusRequestDto request,
                                                @AuthenticationPrincipal UserDetails driverDetails) {
        driverService.updateDriverAvailability(driverDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("me/location")
    public ResponseEntity<?> updateLocation(@RequestBody LocationDto locationDto,
                                            @AuthenticationPrincipal UserDetails driverDetails) {
        log.info("updateLocation in controller called!");
        driverService.updateDriverLocation(driverDetails.getUsername(), locationDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/me/documents")
    public ResponseEntity<String> uploadDocuments(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type")DocumentType documentType,
            @AuthenticationPrincipal UserDetails driverDetails) throws IOException {

            String fileUrl = fileStorageService.uploadFile(file);

            driverService.saveDocumentUrl(driverDetails.getUsername(), fileUrl, documentType);

            return ResponseEntity.ok("Document uploaded successfully. Your application is now under review.");
    }

    @DeleteMapping("/me/documents")
    public ResponseEntity<String> deleteDocuments(
            @RequestBody DocumentDeleteRequestDto request,
            @AuthenticationPrincipal UserDetails driverDetails
            ) {
        fileStorageService.deleteFile(request.getFileUrl());

        driverService.deleteDocumentUrl(request.getFileUrl(), driverDetails.getUsername());

        return ResponseEntity.ok("Document deleted succesfully");
    }

    @PostMapping("/me/vehicles")
    public ResponseEntity<Vehicle> registerVehicle(
            @RequestBody VehicleRegistrationDto request,
            @AuthenticationPrincipal UserDetails driverDetails ){
        Vehicle savedVehicle = driverService.registerVehicle(driverDetails.getUsername(), request);
        return ResponseEntity.ok(savedVehicle);
    }

}
