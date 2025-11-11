package com.Jyotibroto.auradrive.service;

import com.Jyotibroto.auradrive.POJO.DocumentInfo;
import com.Jyotibroto.auradrive.dto.*;
import com.Jyotibroto.auradrive.entity.Trip;
import com.Jyotibroto.auradrive.entity.User;
import com.Jyotibroto.auradrive.entity.Vehicle;
import com.Jyotibroto.auradrive.enums.AccountStatus;
import com.Jyotibroto.auradrive.enums.DocumentType;
import com.Jyotibroto.auradrive.enums.ROLES;
import com.Jyotibroto.auradrive.enums.VehicleType;
import com.Jyotibroto.auradrive.repository.TripRepository;
import com.Jyotibroto.auradrive.repository.UserRepository;
import com.Jyotibroto.auradrive.repository.VehicleRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.*;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DriverService {

    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final VehicleRepository vehicleRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final String DRIVER_LOCATION_KEYS = "DRIVER_LOCATIONS";

    @Autowired
    public DriverService(UserRepository userRepository, RedisTemplate<String, String> redisTemplate, TripRepository tripRepository, SimpMessagingTemplate messagingTemplate, VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
        this.tripRepository = tripRepository;
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    private final String DRIVER_LOCATION_KEY = "driver_locations";
    private final String ACTIVE_VEHICLE_KEY = "driver:active_vehicle";

    public void updateDriverAvailability(String driverEmail, DriverStatusRequestDto request) {
        User driver = userRepository.findByEmail(driverEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Driver not found with email: "+ driverEmail));

        if(request.isAvailable()){
            if(driver.getAccountStatus() != AccountStatus.ACCEPTED) {
                throw new IllegalStateException("Your account is not approved to go online.");
            }
            if(request.getVehicleId() == null || request.getVehicleId().isEmpty()) {
                throw new IllegalArgumentException("Vehicle ID must be provided to go online.");
            }

            Vehicle vehicle = vehicleRepository.findByIdAndDriverId(new ObjectId(request.getVehicleId()), driver.getId())
                    .orElseThrow(() -> new RuntimeException("Vehicle not found or does not belong to driver"));

            String vehicleInfo = vehicle.getId().toString() + ":" + vehicle.getType().name();
            redisTemplate.opsForHash().put(ACTIVE_VEHICLE_KEY, driver.getId().toString(), vehicleInfo);

            driver.setAvailable(true);
            driver.setCurrentVehicleId(vehicle.getId());
            driver.setCurrentVehicleType(vehicle.getType());
        }
        else{
            //Remove active vehicle info from Redis
            redisTemplate.opsForHash().delete(ACTIVE_VEHICLE_KEY, driver.getId().toString());
            //Driver is going offline
            driver.setAvailable(false);
            //Clear current vehicle info
            driver.setCurrentVehicleId(null);
            //Clear current vehicle type
            driver.setCurrentVehicleType(null);
        }
        userRepository.save(driver);

    }

    public List<NearbyDriversResponseDto> findNearByDrivers(LocationDto riderLocation, VehicleType requestedVehicleType) {
        Point searchPoint = new Point(riderLocation.getLongitude(), riderLocation.getLatitude());
        Distance radius = new Distance(5, Metrics.KILOMETERS);
        Circle searchCircle = new Circle(searchPoint, radius);

        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeCoordinates().sortAscending();

        // Fetch active drivers from Redis
        GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults = redisTemplate.opsForGeo().radius(DRIVER_LOCATION_KEYS, searchCircle, args);
        if(geoResults == null || geoResults.getContent().isEmpty()) {
            return Collections.emptyList();
        }

        // Extract driver IDs from geoResults
        List<String> driverIDs = geoResults.getContent().stream()
                .map(geoResult -> geoResult.getContent().getName())
                .toList();
        if(driverIDs.isEmpty()) {
            return Collections.emptyList();
        }

//        List<Object> activeVehicleInfoList = redisTemplate.opsForHash().multiGet(ACTIVE_VEHICLE_KEY, (Collection<Object>)(Collection<?>) driverIDs);

        // Prepare keys for multiGet
        Collection<Object> hashKeys = new ArrayList<>(driverIDs);
        // Fetch active vehicle info from Redis
        List<Object> rawActiveVehicleInfo = redisTemplate.opsForHash().multiGet(ACTIVE_VEHICLE_KEY, hashKeys);

        // Convert raw vehicle info to String list
        List<String> activeVehicleInfoList = rawActiveVehicleInfo.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .toList();


        List<String> matchingDriverIds = new ArrayList<>();
        for(int i = 0; i < driverIDs.size(); i++) {
            String DriverId = driverIDs.get(i);
            if(i < activeVehicleInfoList.size()) {
                String VehicleInfo = activeVehicleInfoList.get(i);
                if(VehicleInfo != null && !VehicleInfo.isEmpty()) {
                    String[] parts = VehicleInfo.split(":");
                    if(parts.length == 2 && parts[1].equals(requestedVehicleType.name())) {
                        matchingDriverIds.add(DriverId);
                    }
                }
            }
        }

        if(matchingDriverIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Convert matching driver IDs to ObjectId list
        List<ObjectId> driverObjectIds = matchingDriverIds.stream().map(ObjectId::new).toList();

        // Fetch driver details from MongoDB
        Map<String, User> driverMap = userRepository.findAllById(driverObjectIds).stream()
                .collect(Collectors.toMap(user -> user.getId().toString(), user -> user));

        return geoResults.getContent().stream()
                .filter(geoResult -> matchingDriverIds.contains(geoResult.getContent().getName()))
                .map(geoResult -> {
                    NearbyDriversResponseDto dto = new NearbyDriversResponseDto();
                    String driverId = geoResult.getContent().getName();
                    User driver = driverMap.get(driverId);

                    dto.setId(driverId);
                    if (driver != null) {
                        dto.setUserName(driver.getUserName());
                        dto.setVehicleType(driver.getCurrentVehicleType()); // Get type from User entity
                    }
                    Point point = geoResult.getContent().getPoint();
                    LocationDto locationDto = new LocationDto();
                    locationDto.setLongitude(point.getX());
                    locationDto.setLatitude(point.getY());
                    dto.setCurrentLocation(locationDto);

                    return dto;
                }).collect(Collectors.toList());

    }

    private NearbyDriversResponseDto mapToNearByDriverDto(GeoResult<RedisGeoCommands.GeoLocation<String>> geoResult) {
        NearbyDriversResponseDto response = new NearbyDriversResponseDto();

        RedisGeoCommands.GeoLocation<String> location = geoResult.getContent();

        response.setId(location.getName());

        Point point = location.getPoint();

        LocationDto locationDto = new LocationDto();
        locationDto.setLongitude(point.getX());
        locationDto.setLatitude(point.getY());
        response.setCurrentLocation(locationDto);

        return response;
    }

    public void updateDriverLocation(String driverEmail, LocationDto locationDto) {
        try{

            log.info("updateLocation in service called for email: {} ", driverEmail);
            User driver = userRepository.findByEmail(driverEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("Driver not found"));

            Point point = new Point(locationDto.getLongitude(), locationDto.getLatitude());
            String driverId = driver.getId().toString();

            long result = redisTemplate.opsForGeo().add(DRIVER_LOCATION_KEYS, point, driverId);
            log.info("Redis GEOADD result for driver {}: {}", driverId, result);
        }
        catch (Exception e) {
            log.error("!!! Exception while updating location in Redis !!!", e);
        }
    }

    public void updateAndBroadcastLocation(String tripId, LocationUpdateDto locationUpdate) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if(trip.getDriverId() != null && trip.getDriverId().toString().equals(locationUpdate.getDriverId())){
            Point point = new Point(locationUpdate.getLongitude(),locationUpdate.getLatitude());
            redisTemplate.opsForGeo().add(DRIVER_LOCATION_KEYS, point, locationUpdate.getDriverId());

            String destination = "/topic/trip/" + tripId;
            messagingTemplate.convertAndSend(destination, locationUpdate);
        }
    }

    public void saveDocumentUrl(String driverEmail, String fileUrl, DocumentType documentType) {
        User driver = userRepository.findByEmail(driverEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Driver not found"));

        DocumentInfo documentInfo = new DocumentInfo();
        documentInfo.setDocumentType(documentType);
        documentInfo.setUrl(fileUrl);
        documentInfo.setUploadedAt(LocalDateTime.now());

        driver.getDocuments().add(documentInfo);
        driver.setAccountStatus(AccountStatus.PENDING_APPROVAL);

        userRepository.save(driver);
    }

    public void deleteDocumentUrl(String fileUrl, String driverEmail) {
        User driver = userRepository.findByEmail(driverEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Driver not found"));

        driver.getDocuments().removeIf(doc -> doc.getUrl().equals(fileUrl));

        if(driver.getDocuments().isEmpty()) {
            driver.setAccountStatus(AccountStatus.PENDING_DOCUMENTS);
        }

        userRepository.save(driver);
    }

    public Vehicle registerVehicle(String driverEmail, VehicleRegistrationDto request) {
        User driver = userRepository.findByEmail(driverEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Driver not found"));

        Vehicle vehicle = new Vehicle();
        vehicle.setDriverId(driver.getId());
        vehicle.setVehicleNumber(request.getVehicleNumber());
        vehicle.setVehicleModel(request.getVehicleModel());
        vehicle.setVehicleColor(request.getVehicleColor());
        vehicle.setType(request.getVehicleType());
        vehicle.setMake(request.getMake());
        vehicle.setActive(true);

        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        driver.getVehicleIds().add(savedVehicle.getId().toString());
        userRepository.save(driver);

        return savedVehicle;
    }
}
