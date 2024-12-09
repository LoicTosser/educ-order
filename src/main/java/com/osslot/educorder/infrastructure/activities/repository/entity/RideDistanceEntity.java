package com.osslot.educorder.infrastructure.activities.repository.entity;

import com.osslot.educorder.domain.activities.model.RideDistance;
import com.osslot.educorder.infrastructure.common.repository.entity.MultiTenantEntity;
import com.osslot.educorder.infrastructure.user.repository.entity.LocationEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class RideDistanceEntity extends MultiTenantEntity {

  private static final String CURRENT_VERSION = "0.0.1";
  public static final String PATH = "ride_distances";

  private LocationEntity from;
  private LocationEntity to;
  private Long distanceInKilometers;

  public static RideDistanceEntity fromDomain(RideDistance rideDistance) {
    return RideDistanceEntity.builder()
        .id(rideDistance.rideDistanceId().id())
        .version(CURRENT_VERSION)
        .from(LocationEntity.fromDomain(rideDistance.ride().from()))
        .to(LocationEntity.fromDomain(rideDistance.ride().to()))
        .distanceInKilometers(rideDistance.distanceInMeters())
        .build();
  }

  public RideDistance toDomain() {
    return new RideDistance(
        new RideDistance.RideDistanceId(getId()),
        new RideDistance.Ride(from.toDomain(), to.toDomain()),
        distanceInKilometers);
  }
}
