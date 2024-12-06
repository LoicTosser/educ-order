package com.osslot.educorder.domain.activities.repository;

import com.osslot.educorder.domain.activities.model.Location;
import com.osslot.educorder.domain.user.model.User.UserId;
import java.util.List;
import java.util.Optional;

public interface LocationRepository {

  List<Location> findAll(UserId userId);

  Optional<Location> findByName(UserId userId, String name);

  Optional<Location> findByAddress(UserId userId, String address);
}
