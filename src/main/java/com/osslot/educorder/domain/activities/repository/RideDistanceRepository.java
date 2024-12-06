package com.osslot.educorder.domain.activities.repository;

import com.osslot.educorder.domain.activities.model.Location;
import com.osslot.educorder.domain.activities.model.RideDistance;
import java.util.Optional;

public interface RideDistanceRepository {

  Optional<RideDistance> findDistanceBetween(Location from, Location to);
}
