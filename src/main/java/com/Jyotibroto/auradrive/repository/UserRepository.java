package com.Jyotibroto.auradrive.repository;

import com.Jyotibroto.auradrive.entity.User;
import com.Jyotibroto.auradrive.enums.AccountStatus;
import com.Jyotibroto.auradrive.enums.ROLES;
import org.bson.types.ObjectId;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, ObjectId> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUserName(String userName);

    List<User> findByAccountStatus(AccountStatus status);

    List<User> findByRoleAndAvailableTrueAndCurrentLocationNear(ROLES roles, Point location, Distance distance);
}
