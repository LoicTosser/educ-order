package com.osslot.educorder.domain.activities.service;

import com.osslot.educorder.domain.activities.model.ActivitySyncToken;
import com.osslot.educorder.domain.activities.repository.ActivityRepository;
import com.osslot.educorder.domain.activities.repository.ActivitySyncTokenRepository;
import com.osslot.educorder.domain.activities.repository.CalendarRepository;
import com.osslot.educorder.domain.user.model.User.UserId;
import com.osslot.educorder.domain.user.model.UserSettings.GoogleCalendarSettings.CalendarId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ImportActivitiesService {

  private final CalendarRepository calendarRepository;
  private final ActivityRepository fireStoreActivityRepository;
  //  private final ActivityRepository googleSheetActivityRepository;
  private final ActivitySyncTokenRepository activitySyncTokenRepository;

  public CalendarRepository.FetchCalendarActivitiesResponse importActivities(
      UserId userId, CalendarId calendarId, int year, int month) {
    var fetchCalendarActivitiesResponse =
        calendarRepository.fromCalendar(userId, calendarId, year, month);
    fireStoreActivityRepository.add(fetchCalendarActivitiesResponse.activities());
    return fetchCalendarActivitiesResponse;
  }

  public CalendarRepository.FetchCalendarActivitiesResponse importActivitiesToGoogleSheet(
      UserId userId, CalendarId calendarId, ZonedDateTime start, ZonedDateTime end) {
    var fetchCalendarActivitiesResponse = fetchCalendarActivities(userId, calendarId, start, end);
    //    googleSheetActivityRepository.add(fetchCalendarActivitiesResponse.activities());
    return fetchCalendarActivitiesResponse;
  }

  public CalendarRepository.FetchCalendarActivitiesResponse importActivities(
      UserId userId, CalendarId calendarId, ZonedDateTime start, ZonedDateTime end) {
    var fetchCalendarActivitiesResponse = fetchCalendarActivities(userId, calendarId, start, end);
    fireStoreActivityRepository.add(fetchCalendarActivitiesResponse.activities());
    return fetchCalendarActivitiesResponse;
  }

  private CalendarRepository.FetchCalendarActivitiesResponse fetchCalendarActivities(
      UserId userId, CalendarId calendarId, ZonedDateTime start, ZonedDateTime end) {
    return calendarRepository.fromCalendar(userId, calendarId, start, end);
  }

  public void syncActivities(List<SynchronizeCalendarRequest> synchronizeCalendarRequests) {
    synchronizeCalendarRequests.forEach(
        synchronizeCalendarRequest -> {
          log.info(
              "Syncing activities from calendar for user {}", synchronizeCalendarRequest.userId());
          var nextSyncToken =
              activitySyncTokenRepository.getCurrentActivitySyncToken(
                  synchronizeCalendarRequest.userId());
          if (nextSyncToken.isEmpty()
              || nextSyncToken.get().syncToken() == null
              || nextSyncToken.get().syncToken().isEmpty()) {
            log.info("Sync token is null, first init");
            init(synchronizeCalendarRequest.withNextSyncToken(nextSyncToken.orElse(null)));
            return;
          }

          var fetchResult =
              synchronize(synchronizeCalendarRequest.withNextSyncToken(nextSyncToken.get()));
          activitySyncTokenRepository.setCurrentActivitySyncToken(
              synchronizeCalendarRequest.userId(),
              new ActivitySyncToken(
                  nextSyncToken.get().id(),
                  synchronizeCalendarRequest.userId(),
                  fetchResult.nextSyncToken()));
          log.info("Sync done, next sync token: {}", fetchResult.nextSyncToken());
        });
  }

  private CalendarRepository.FetchCalendarActivitiesResponse synchronize(
      SynchronizeCalendarRequest synchronizeCalendarRequest) {
    var fetchCalendarActivitiesResponse =
        calendarRepository.fromLastSync(
            synchronizeCalendarRequest.userId(),
            synchronizeCalendarRequest.calendarId(),
            synchronizeCalendarRequest.nextSyncToken());
    fireStoreActivityRepository.synchronyze(fetchCalendarActivitiesResponse.activities());
    return fetchCalendarActivitiesResponse;
  }

  private void init(SynchronizeCalendarRequest synchronizeCalendarRequest) {
    ZonedDateTime start = fromDateTime();
    var end = start.plusYears(1);
    var fetchResult =
        importActivities(
            synchronizeCalendarRequest.userId(),
            synchronizeCalendarRequest.calendarId(),
            start,
            end);

    log.info("Initial import of activities done, next sync token: {}", fetchResult.nextSyncToken());
    activitySyncTokenRepository.setCurrentActivitySyncToken(
        synchronizeCalendarRequest.userId(),
        new ActivitySyncToken(
            synchronizeCalendarRequest.nextSyncToken() == null
                ? new ActivitySyncToken.ActivitySyncTokenId(UUID.randomUUID().toString())
                : synchronizeCalendarRequest.nextSyncToken().id(),
            synchronizeCalendarRequest.userId(),
            fetchResult.nextSyncToken()));
  }

  private static @NotNull ZonedDateTime fromDateTime() {
    return ZonedDateTime.of(2024, 10, 1, 0, 0, 0, 0, ZonedDateTime.now().getZone());
  }

  @With
  public record SynchronizeCalendarRequest(
      UserId userId, CalendarId calendarId, ActivitySyncToken nextSyncToken) {}
}
