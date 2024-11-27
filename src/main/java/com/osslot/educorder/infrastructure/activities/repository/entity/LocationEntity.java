package com.osslot.educorder.infrastructure.activities.repository.entity;

import com.osslot.educorder.domain.activities.model.Location;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class LocationEntity {

  private String name;
  private String address;

  public static LocationEntity fromDomain(Location location) {
    return new LocationEntity(location.name(), location.address());
  }

  public Location toDomain() {
    return new Location(name, address);
  }
}
