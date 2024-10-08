package com.osslot.educorder.domain.repository;

import com.osslot.educorder.domain.model.Location;
import java.util.Optional;

public interface RideDistanceRepository {

  Optional<Long> getDistanceInKilometers(Location from, Location to);
}
