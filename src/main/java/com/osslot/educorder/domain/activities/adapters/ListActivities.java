package com.osslot.educorder.domain.activities.adapters;

import com.osslot.educorder.domain.activities.model.Activity;
import com.osslot.educorder.domain.activities.repository.ActivityRepository;
import com.osslot.educorder.domain.user.model.User.UserId;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ListActivities {

  private final ActivityRepository fireStoreActivityRepository;

  public List<Activity> listActivities(UserId userId, ZonedDateTime start, ZonedDateTime end) {
    return fireStoreActivityRepository.findAllBetween(userId, start, end);
  }
}
