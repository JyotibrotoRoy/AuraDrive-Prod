package com.Jyotibroto.auradrive.repository;

import com.Jyotibroto.auradrive.entity.Trip;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TripRepository extends MongoRepository<Trip, String> {
    List<Trip> findTop5ByRiderIdOrderByCreatedAtDesc(ObjectId riderId, ObjectId driverId);
}
