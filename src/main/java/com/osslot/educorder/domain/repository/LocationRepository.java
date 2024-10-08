package com.osslot.educorder.domain.repository;

import com.osslot.educorder.domain.model.Location;
import java.util.List;
import java.util.Optional;

public interface LocationRepository {

  List<Location> findAll();

  Optional<Location> findByName(String name);

  Optional<Location> findByAddress(String address);
}
