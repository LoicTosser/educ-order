package com.osslot.educorder.infrastructure.user.repository.entity;

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

  private static final String CURRENT_VERSION = "0.0.1";

  private String version;
  private String name;
  private String address;

  public static LocationEntity fromDomain(Location location) {
    return new LocationEntity(CURRENT_VERSION, location.name(), location.address());
  }

  public Location toDomain() {
    return new Location(name, address);
  }
}
