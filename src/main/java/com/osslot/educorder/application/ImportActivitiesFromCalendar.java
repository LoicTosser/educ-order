package com.osslot.educorder.application;

import com.osslot.educorder.domain.model.ActivitySyncToken;
import com.osslot.educorder.domain.model.UserSettings;
import com.osslot.educorder.domain.model.UserSettings.User;
import com.osslot.educorder.domain.repository.ActivityRepository;
import com.osslot.educorder.domain.repository.ActivitySyncTokenRepository;
import com.osslot.educorder.domain.repository.CalendarRepository;
import com.osslot.educorder.domain.repository.CalendarRepository.FetchCalendarActivitiesResponse;
import com.osslot.educorder.domain.repository.UserSettingsRepository;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ImportActivitiesFromCalendar {

  private final CalendarRepository calendarRepository;
  private final ActivityRepository fireStoreActivityRepository;
  private final ActivityRepository googleSheetActivityRepository;
  private final UserSettingsRepository userSettingsRepository;
  private final ActivitySyncTokenRepository activitySyncTokenRepository;

  public FetchCalendarActivitiesResponse importActivities(String userId, int year, int month) {
    var userSettings = userSettingsRepository.findByUserId(userId);
    if (userSettings.isEmpty()) {
      return new FetchCalendarActivitiesResponse(List.of(), null, new User(userId, null, null));
    }
    var calendarId = userSettings.orElseThrow().googleCalendarSettings().calendarId();
    var fetchCalendarActivitiesResponse =
        calendarRepository.fromCalendar(userSettings.orElseThrow().user(), calendarId, year, month);
    fireStoreActivityRepository.add(fetchCalendarActivitiesResponse.activities());
    return fetchCalendarActivitiesResponse;
  }

  public FetchCalendarActivitiesResponse importActivitiesToGoogleSheet(
      String userId, ZonedDateTime start, ZonedDateTime end) {
    var fetchCalendarActivitiesResponse = fetchCalendarActivities(userId, start, end);
    googleSheetActivityRepository.add(fetchCalendarActivitiesResponse.activities());
    return fetchCalendarActivitiesResponse;
  }

  public FetchCalendarActivitiesResponse importActivities(
      String userId, ZonedDateTime start, ZonedDateTime end) {
    var fetchCalendarActivitiesResponse = fetchCalendarActivities(userId, start, end);
    fireStoreActivityRepository.add(fetchCalendarActivitiesResponse.activities());
    return fetchCalendarActivitiesResponse;
  }

  private FetchCalendarActivitiesResponse fetchCalendarActivities(
      String userId, ZonedDateTime start, ZonedDateTime end) {
    var userSettings = userSettingsRepository.findByUserId(userId);
    if (userSettings.isEmpty()) {
      return new FetchCalendarActivitiesResponse(List.of(), null, new User(userId, null, null));
    }
    var calendarId = userSettings.orElseThrow().googleCalendarSettings().calendarId();
    return calendarRepository.fromCalendar(
        userSettings.orElseThrow().user(), calendarId, start, end);
  }

  public void syncActivities() {
    var usersSettings = userSettingsRepository.findByGoogleCalendarSynchroEnabled(true);
    usersSettings.forEach(
        userSettings -> {
          log.info("Syncing activities from calendar for user {}", userSettings.user());
          var nextSyncToken =
              activitySyncTokenRepository.getCurrentActivitySyncToken(userSettings.user());
          if (nextSyncToken.isEmpty()
              || nextSyncToken.get().syncToken() == null
              || nextSyncToken.get().syncToken().isEmpty()) {
            log.info("Sync token is null, first init");
            init(userSettings);
            return;
          }
          var fetchResult = synchronize(userSettings, nextSyncToken.get());
          activitySyncTokenRepository.setCurrentActivitySyncToken(
              userSettings.user(),
              new ActivitySyncToken(userSettings.user().id(), fetchResult.nextSyncToken()));
          log.info("Sync done, next sync token: {}", fetchResult.nextSyncToken());
        });
  }

  private FetchCalendarActivitiesResponse synchronize(
      UserSettings userSettings, ActivitySyncToken nextSyncToken) {
    var calendarId = userSettings.googleCalendarSettings().calendarId();
    var fetchCalendarActivitiesResponse =
        calendarRepository.fromLastSync(userSettings.user(), calendarId, nextSyncToken);
    fireStoreActivityRepository.synchronyze(fetchCalendarActivitiesResponse.activities());
    return fetchCalendarActivitiesResponse;
  }

  private void init(UserSettings userSettings) {
    ZonedDateTime start = fromDateTime();
    var end = start.plusYears(1);
    var fetchResult = importActivities(userSettings.user().id(), start, end);

    log.info("Initial import of activities done, next sync token: {}", fetchResult.nextSyncToken());
    activitySyncTokenRepository.setCurrentActivitySyncToken(
        userSettings.user(),
        new ActivitySyncToken(userSettings.user().id(), fetchResult.nextSyncToken()));
  }

  private static @NotNull ZonedDateTime fromDateTime() {
    return ZonedDateTime.of(2024, 10, 1, 0, 0, 0, 0, ZonedDateTime.now().getZone());
  }
}
