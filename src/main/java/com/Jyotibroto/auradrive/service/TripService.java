package com.Jyotibroto.auradrive.service;

import com.Jyotibroto.auradrive.dto.LocationDto;
import com.Jyotibroto.auradrive.dto.TripRequestDto;
import com.Jyotibroto.auradrive.dto.TripResponseDto;
import com.Jyotibroto.auradrive.entity.Trip;
import com.Jyotibroto.auradrive.entity.User;
import com.Jyotibroto.auradrive.enums.AccountStatus;
import com.Jyotibroto.auradrive.enums.TripStatus;
import com.Jyotibroto.auradrive.repository.TripRepository;
import com.Jyotibroto.auradrive.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class TripService {
    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private FareService fareService;


    public TripResponseDto createTrip(TripRequestDto request, UserDetails riderDetails) {
            User rider = userRepository.findByEmail(riderDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("Rider not found"));

            Trip trip = new Trip();
            trip.setRiderId(rider.getId());
            trip.setStartLocation(new GeoJsonPoint(request.getStartLocation().getLongitude(), request.getStartLocation().getLatitude()));
            trip.setEndLocation(new GeoJsonPoint(request.getEndLocation().getLongitude(), request.getEndLocation().getLatitude()));
            trip.setTripStatus(TripStatus.REQUESTED);
            trip.setCreatedAt(LocalDateTime.now());

            trip.setVehicleType(request.getVehicleType());

            TripResponseDto response = new TripResponseDto();
            response.setVehicleType(trip.getVehicleType());
            Trip savedTrip = tripRepository.save(trip);

            LocationDto start = new LocationDto();
            start.setLongitude(savedTrip.getStartLocation().getX());
            start.setLatitude(savedTrip.getStartLocation().getY());
            response.setStartLocation(start);

            LocationDto end = new LocationDto();
            end.setLongitude(savedTrip.getEndLocation().getX());
            end.setLatitude(savedTrip.getEndLocation().getY());
            response.setEndLocation(end);

            response.setId(savedTrip.getId());
            response.setRiderId(savedTrip.getRiderId().toString());
            response.setStatus(savedTrip.getTripStatus());
            response.setCreatedAt(savedTrip.getCreatedAt());

            return response;
        }

        public TripResponseDto acceptTrip(String tripId, UserDetails driverDetails) {
            Trip trip = tripRepository.findById(tripId)
                    .orElseThrow(() -> new RuntimeException("Trip not found with id: " + tripId));

            if(trip.getTripStatus() != TripStatus.REQUESTED) {
                throw new IllegalStateException("Trip is not a requested state and cannot be accepted");
            }

            User driver = userRepository.findByEmail(driverDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("Driver not found"));

            if(driver.getAccountStatus() != AccountStatus.ACCEPTED) {
                throw new SecurityException("Your account is not approved to be accepted");
            }

            String otp = new DecimalFormat("0000").format(new Random().nextInt(9999));
            trip.setOtp(passwordEncoder.encode(otp));

            if(!driver.isAvailable()) {
                throw new IllegalStateException("Driver is not available for a new trip");
            }

            driver.setAvailable(false);
            userRepository.save(driver);

            trip.setDriverId(driver.getId());
            trip.setTripStatus(TripStatus.ACCEPTED);
            Trip updatedTrip = tripRepository.save(trip);
            TripResponseDto response = mapToTripResponseDto(updatedTrip);
            response.setOtpForRider(otp);

            String destination = "/topic/trip/" + tripId;

            simpMessagingTemplate.convertAndSend(destination, response);

            return response;
        }

        public TripResponseDto cancelTrip(String tripId, UserDetails userDetails) {
            Trip trip = tripRepository.findById(tripId)
                    .orElseThrow(() -> new RuntimeException("Trip not found with trip id: " + tripId));
            User currentUser = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            if(!trip.getRiderId().equals(currentUser.getId()) && (trip.getDriverId() == null || !trip.getDriverId().equals(currentUser.getId()))) {
                throw new SecurityException("User is not Authorized to cancel the trip");
            }

            currentUser.setAvailable(true);
            userRepository.save(currentUser);

            trip.setTripStatus(TripStatus.CANCELLED);
            Trip updatedTrip = tripRepository.save(trip);
            TripResponseDto response = mapToTripResponseDto(updatedTrip);

            String destination = "/topic/trip/" + tripId;

            simpMessagingTemplate.convertAndSend(destination, response);

            return response;
        }

        public TripResponseDto startTrip(String tripId, String otp, UserDetails driverDetails) {
            Trip trip = tripRepository.findById(tripId)
                    .orElseThrow(() -> new RuntimeException("Trip not found"));

            User driver = userRepository.findByEmail(driverDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("Driver not found"));

            if(trip.getDriverId() == null || !trip.getDriverId().equals(driver.getId())) {
                throw new SecurityException("You are not authorized to start this trip");
            }

            if(!passwordEncoder.matches(otp, trip.getOtp())) {
                throw new SecurityException("Invalid OTP");
            }

            trip.setTripStatus(TripStatus.IN_PROGRESS);
            trip.setCreatedAt(LocalDateTime.now());
            Trip updatedTrip = tripRepository.save(trip);

            simpMessagingTemplate.convertAndSend("/topic/trip/" + tripId, mapToTripResponseDto(updatedTrip));

            return mapToTripResponseDto(updatedTrip);
        }

        @Transactional
        public TripResponseDto endTrip(String tripId, UserDetails driverDetails) {
            Trip trip = tripRepository.findById(tripId)
                    .orElseThrow(() -> new RuntimeException("Trip not found"));

            User driver = userRepository.findByEmail(driverDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("Driver Not Found"));

            if(trip.getDriverId() == null || !trip.getDriverId().equals(driver.getId())) {
                throw new SecurityException("User is not Authorized to end the trip");
            }

            if(trip.getTripStatus() != TripStatus.IN_PROGRESS) {
                throw new IllegalStateException("Trip must be in progress to be ended.");
            }

            trip.setEndedAt(LocalDateTime.now());
            double finalFare = fareService.calculateFare(trip);
            trip.setFinalFare(finalFare);// Placeholder for fare calculation logic

            driver.setAvailable(true);
            userRepository.save(driver);

            trip.setTripStatus(TripStatus.COMPLETED);
            Trip updatedTrip = tripRepository.save(trip);
            TripResponseDto response = mapToTripResponseDto(updatedTrip);

            String destination = "/topic/trip/" + tripId;

            simpMessagingTemplate.convertAndSend(destination, response);

            return response;
        }

        public TripResponseDto mapToTripResponseDto(Trip trip) {
            TripResponseDto response = new TripResponseDto();
            response.setId(trip.getId());
            response.setRiderId(trip.getRiderId().toString());
            if(trip.getDriverId() != null ) {
                response.setDiverId(trip.getDriverId().toString());
            }

            response.setStatus(trip.getTripStatus());
            response.setCreatedAt(trip.getCreatedAt());

            LocationDto start = new LocationDto();
            start.setLongitude(trip.getStartLocation().getX());
            start.setLatitude(trip.getEndLocation().getY());
            response.setStartLocation(start);

            LocationDto end = new LocationDto();
            end.setLongitude(trip.getEndLocation().getX());
            end.setLatitude(trip.getEndLocation().getY());
            response.setEndLocation(end);

            return response;
        }

        public List<TripResponseDto> getTripHistory(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            ObjectId userId = user.getId();

        List<Trip> trips = tripRepository.findTop5ByRiderIdOrderByCreatedAtDesc(userId, userId);

        return trips.stream()
                .map(this::mapToTripResponseDto)
                .collect(Collectors.toList());
        }
}
