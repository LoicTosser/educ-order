package com.osslot.educorder.infrastructure.activities.repository;

import com.google.cloud.firestore.Firestore;
import com.osslot.educorder.domain.activities.model.Location;
import com.osslot.educorder.domain.activities.model.RideDistance;
import com.osslot.educorder.domain.activities.model.RideDistance.Ride;
import com.osslot.educorder.domain.activities.repository.RideDistanceRepository;
import com.osslot.educorder.infrastructure.activities.repository.entity.RideDistanceEntity;
import com.osslot.educorder.infrastructure.activities.service.RideDistanceService;
import com.osslot.educorder.infrastructure.user.repository.entity.LocationEntity;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class FirestoreRideDistanceRepository implements RideDistanceRepository {

  private final Map<Ride, RideDistance> rideDistancesCache = new ConcurrentHashMap<>();

  private final Firestore firestore;
  private final RideDistanceService rideDistanceService;

  @Override
  public Optional<RideDistance> findDistanceBetween(Location from, Location to) {
    return Optional.ofNullable(
        rideDistancesCache.computeIfAbsent(new Ride(from, to), this::findRideDistanceFor));
  }

  private RideDistance findRideDistanceFor(Ride ride) {
    try {
      var rideDistanceEntities =
          firestore
              .collection(RideDistanceEntity.PATH)
              .whereEqualTo("from", LocationEntity.fromDomain(ride.from()))
              .whereEqualTo("to", LocationEntity.fromDomain(ride.to()))
              .get()
              .get()
              .toObjects(RideDistanceEntity.class);
      if (rideDistanceEntities.isEmpty()) {
        var rideDistance = rideDistanceService.calculateDistance(ride);
        rideDistance.ifPresentOrElse(
            this::addRideDistanceAndOpposite,
            () -> log.info("No ride distance calculated for ride {}", ride));
        return rideDistance.orElse(null);
      }
      return rideDistanceEntities.getFirst().toDomain();
    } catch (InterruptedException | ExecutionException e) {
      log.error("Error fetching ride distance for ride {}", ride, e);
      return null;
    }
  }

  private void addRideDistanceAndOpposite(RideDistance rideDistance) {
    add(rideDistance);
    add(
        new RideDistance(
            new RideDistance.RideDistanceId(UUID.randomUUID().toString()),
            rideDistance.ride().opposite(),
            rideDistance.distanceInMeters()));
  }

  private void add(RideDistance rideDistance) {
    var rideDistanceEntity = RideDistanceEntity.fromDomain(rideDistance);
    try {
      firestore.collection(RideDistanceEntity.PATH).document().set(rideDistanceEntity).get();
    } catch (InterruptedException | ExecutionException e) {
      log.error("Error adding ride distance", e);
    }
  }
}
