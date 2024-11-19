package com.osslot.educorder.domain.activities.repository;

import com.osslot.educorder.domain.activities.model.Activity;
import com.osslot.educorder.domain.activities.model.ActivitySyncToken;
import com.osslot.educorder.domain.model.User.UserId;
import com.osslot.educorder.domain.model.UserSettings.GoogleCalendarSettings.CalendarId;
import java.time.ZonedDateTime;
import java.util.List;

public interface CalendarRepository {

  FetchCalendarActivitiesResponse fromCalendar(
      UserId userId, CalendarId calendarId, int year, int month);

  FetchCalendarActivitiesResponse fromCalendar(
      UserId userId, CalendarId calendarId, ZonedDateTime start, ZonedDateTime end);

  FetchCalendarActivitiesResponse fromLastSync(
      UserId userId, CalendarId calendarId, ActivitySyncToken nextSyncToken);

  record FetchCalendarActivitiesResponse(
      List<Activity> activities, String nextSyncToken, UserId userId) {}
}
