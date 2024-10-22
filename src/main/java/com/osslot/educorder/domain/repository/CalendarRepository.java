package com.osslot.educorder.domain.repository;

import com.osslot.educorder.domain.model.Activity;
import com.osslot.educorder.domain.model.ActivitySyncToken;
import com.osslot.educorder.domain.model.UserSettings.User;
import java.time.ZonedDateTime;
import java.util.List;

public interface CalendarRepository {

  FetchCalendarActivitiesResponse fromCalendar(User user, String calendarId, int year, int month);

  FetchCalendarActivitiesResponse fromCalendar(
      User user, String calendarId, ZonedDateTime start, ZonedDateTime end);

  FetchCalendarActivitiesResponse fromLastSync(
      User user, String calendarId, ActivitySyncToken nextSyncToken);

  record FetchCalendarActivitiesResponse(
      List<Activity> activities, String nextSyncToken, User user) {}
}
