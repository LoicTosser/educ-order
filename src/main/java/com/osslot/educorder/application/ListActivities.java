package com.osslot.educorder.application;

import com.osslot.educorder.domain.model.Activity;
import com.osslot.educorder.domain.model.UserSettings.User;
import com.osslot.educorder.domain.repository.ActivityRepository;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ListActivities {

  private final ActivityRepository fireStoreActivityRepository;

  public List<Activity> listActivities(User user, ZonedDateTime start, ZonedDateTime end) {
    return fireStoreActivityRepository.findAllBetween(user, start, end);
  }
}
