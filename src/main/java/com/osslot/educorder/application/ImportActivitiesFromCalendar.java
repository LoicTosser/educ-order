package com.osslot.educorder.application;

import com.osslot.educorder.domain.repository.ActivityRepository;
import com.osslot.educorder.domain.repository.CalendarRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ImportActivitiesFromCalendar {

  private final CalendarRepository calendarRepository;
  private final ActivityRepository activityRepository;

  public void importActivities(int year, int month) {
    var activities = calendarRepository.fromCalendar(year, month);
    activityRepository.add(activities);
  }
}
