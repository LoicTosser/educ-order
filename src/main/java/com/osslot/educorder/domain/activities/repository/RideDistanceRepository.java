package com.osslot.educorder.domain.activities.repository;

import com.osslot.educorder.domain.activities.model.Location;
import java.util.Optional;

public interface RideDistanceRepository {

  Optional<Long> getDistanceInKilometers(Location from, Location to);
}
