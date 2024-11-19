package com.osslot.educorder.domain.activities.adapters;

import static com.osslot.educorder.domain.model.User.UserId;

import com.osslot.educorder.domain.activities.repository.CalendarRepository.FetchCalendarActivitiesResponse;
import com.osslot.educorder.domain.activities.service.ImportActivitiesService;
import com.osslot.educorder.domain.activities.service.ImportActivitiesService.SynchronizeCalendarRequest;
import com.osslot.educorder.domain.user.adapters.UserSettingsAdapter;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ImportActivitiesFromCalendar {

  private final ImportActivitiesService importActivitiesService;
  private final UserSettingsAdapter userSettingsAdapter;

  public FetchCalendarActivitiesResponse importActivities(UserId userId, int year, int month) {
    var userSettings = userSettingsAdapter.findByUserId(userId);
    if (userSettings.isEmpty()) {
      return new FetchCalendarActivitiesResponse(List.of(), null, userId);
    }
    var calendarId = userSettings.orElseThrow().googleCalendarSettings().calendarId();
    return importActivitiesService.importActivities(userId, calendarId, year, month);
  }

  public FetchCalendarActivitiesResponse importActivitiesToGoogleSheet(
      UserId userId, ZonedDateTime start, ZonedDateTime end) {
    var userSettings = userSettingsAdapter.findByUserId(userId);
    if (userSettings.isEmpty()) {
      return new FetchCalendarActivitiesResponse(List.of(), null, userId);
    }
    var calendarId = userSettings.orElseThrow().googleCalendarSettings().calendarId();
    return importActivitiesService.importActivitiesToGoogleSheet(userId, calendarId, start, end);
  }

  public FetchCalendarActivitiesResponse importActivities(
      UserId userId, ZonedDateTime start, ZonedDateTime end) {
    var userSettings = userSettingsAdapter.findByUserId(userId);
    if (userSettings.isEmpty()) {
      return new FetchCalendarActivitiesResponse(List.of(), null, userId);
    }
    var calendarId = userSettings.orElseThrow().googleCalendarSettings().calendarId();
    return importActivitiesService.importActivities(userId, calendarId, start, end);
  }

  public void syncActivities() {
    var usersSettings = userSettingsAdapter.findByGoogleCalendarSynchroEnabled(true);
    var synchronizeCalendarRequests =
        usersSettings.stream()
            .map(
                userSettings ->
                    new SynchronizeCalendarRequest(
                        userSettings.userId(),
                        userSettings.googleCalendarSettings().calendarId(),
                        null))
            .toList();
    importActivitiesService.syncActivities(synchronizeCalendarRequests);
  }
}
