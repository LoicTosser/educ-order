package com.osslot.educorder.domain.repository;

import com.osslot.educorder.domain.model.Activity;
import java.time.ZonedDateTime;
import java.util.List;

public interface CalendarRepository {

  FetchCalendarActivitiesResponse fromCalendar(int year, int month);

  FetchCalendarActivitiesResponse fromCalendar(ZonedDateTime start, ZonedDateTime end);

  FetchCalendarActivitiesResponse fromLastSync(String nextSyncToken);

  record FetchCalendarActivitiesResponse(List<Activity> activities, String nextSyncToken) {}
}
