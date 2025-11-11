package com.Jyotibroto.auradrive.repository;

import com.Jyotibroto.auradrive.entity.Vehicle;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface VehicleRepository extends MongoRepository<Vehicle, ObjectId> {
    Optional<Vehicle> findByIdAndDriverId(ObjectId id, ObjectId driverId);
}
