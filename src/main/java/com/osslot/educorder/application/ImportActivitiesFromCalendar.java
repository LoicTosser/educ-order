package com.osslot.educorder.application;

import com.osslot.educorder.domain.repository.ActivityRepository;
import com.osslot.educorder.domain.repository.CalendarRepository;
import com.osslot.educorder.domain.repository.CalendarRepository.FetchCalendarActivitiesResponse;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ImportActivitiesFromCalendar {

  private final CalendarRepository calendarRepository;
  private final ActivityRepository fireStoreActivityRepository;

  public FetchCalendarActivitiesResponse importActivities(int year, int month) {
    var fetchCalendarActivitiesResponse = calendarRepository.fromCalendar(year, month);
    fireStoreActivityRepository.add(fetchCalendarActivitiesResponse.activities());
    return fetchCalendarActivitiesResponse;
  }

  public FetchCalendarActivitiesResponse importActivities(ZonedDateTime start, ZonedDateTime end) {
    var fetchCalendarActivitiesResponse = calendarRepository.fromCalendar(start, end);
    fireStoreActivityRepository.add(fetchCalendarActivitiesResponse.activities());
    return fetchCalendarActivitiesResponse;
  }

  public FetchCalendarActivitiesResponse synchronize(String nextSyncToken) {
    var fetchCalendarActivitiesResponse = calendarRepository.fromLastSync(nextSyncToken);
    fireStoreActivityRepository.synchronyze(fetchCalendarActivitiesResponse.activities());
    return fetchCalendarActivitiesResponse;
  }
}
