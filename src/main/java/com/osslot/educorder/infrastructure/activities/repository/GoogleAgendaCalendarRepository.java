package com.osslot.educorder.infrastructure.activities.repository;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.osslot.educorder.domain.activities.model.Activity;
import com.osslot.educorder.domain.activities.model.ActivitySyncToken;
import com.osslot.educorder.domain.activities.repository.CalendarRepository;
import com.osslot.educorder.domain.user.model.User.UserId;
import com.osslot.educorder.domain.user.model.UserSettings.GoogleCalendarSettings.CalendarId;
import com.osslot.educorder.infrastructure.activities.repository.mapper.ActivityMapper;
import com.osslot.educorder.infrastructure.activities.repository.mapper.EventDateTimeMapper;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Month;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GoogleAgendaCalendarRepository implements CalendarRepository {

  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  private final GoogleCredentialsService googleCredentialsService;
  private final ActivityMapper activityMapper;

  public GoogleAgendaCalendarRepository(
      GoogleCredentialsService googleCredentialsService, ActivityMapper activityMapper) {
    this.activityMapper = activityMapper;
    this.googleCredentialsService = googleCredentialsService;
  }

  @Override
  public FetchCalendarActivitiesResponse fromCalendar(
      UserId userId, CalendarId calendarId, int year, int month) {
    var events = getEvents(userId, calendarId, year, month);
    return toFetchCalendarActivitiesResponse(userId, events);
  }

  @Override
  public FetchCalendarActivitiesResponse fromCalendar(
      UserId userId, CalendarId calendarId, ZonedDateTime start, ZonedDateTime end) {
    var events = getEvents(userId, calendarId, start, end);
    return toFetchCalendarActivitiesResponse(userId, events);
  }

  @Override
  public FetchCalendarActivitiesResponse fromLastSync(
      UserId userId, CalendarId calendarId, ActivitySyncToken syncToken) {
    var events = getEvents(userId, calendarId, syncToken.syncToken());
    FetchCalendarActivitiesResponse allActivities =
        toFetchCalendarActivitiesResponse(userId, events);
    return new FetchCalendarActivitiesResponse(
        allActivities.activities().stream()
            .filter(
                activity ->
                    activity
                        .beginDate()
                        .isAfter(
                            ChronoZonedDateTime.from(
                                ZonedDateTime.of(
                                    2024, 10, 1, 0, 0, 0, 0, ZonedDateTime.now().getZone()))))
            .toList(),
        events.nextSyncToken(),
        userId);
  }

  private @NotNull FetchCalendarActivitiesResponse toFetchCalendarActivitiesResponse(
      UserId userId, EventsResponse events) {
    return new FetchCalendarActivitiesResponse(
        events.items().stream()
            .map(event -> activityMapper.fromEvent(userId, event))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .sorted(Comparator.comparing(Activity::beginDate))
            .toList(),
        events.nextSyncToken(),
        userId);
  }

  private EventsResponse getEvents(UserId user, CalendarId calendarId, int year, int month) {
    var dateFrom = getFirstDayOfMonth(year, month);
    var dateTo = getNextMonthFirstDay(year, month, dateFrom);
    return getEvents(user, calendarId, dateFrom, dateTo);
  }

  @NotNull
  private EventsResponse getEvents(
      UserId userId, CalendarId calendarId, ZonedDateTime start, ZonedDateTime end) {
    final NetHttpTransport httpTransport;
    try {
      httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    } catch (GeneralSecurityException | IOException e) {
      return new EventsResponse(List.of(), "");
    }
    Optional<Credential> credentials = googleCredentialsService.getCredentials(userId);
    if (credentials.isEmpty()) {
      return new EventsResponse(List.of(), "");
    }
    var service =
        new Calendar.Builder(httpTransport, JSON_FACTORY, credentials.orElseThrow()).build();
    List<Event> events = new ArrayList<>();
    var nextSyncToken = "";
    try {
      // Call the Calendar API
      log.info("Getting the upcoming 250 events");
      String pageToken = null;
      do {
        var eventsResponse =
            service
                .events()
                .list(calendarId.id())
                .setTimeMin(EventDateTimeMapper.fromZonedDateTime(start))
                .setTimeMax(end == null ? null : EventDateTimeMapper.fromZonedDateTime(end))
                .setPageToken(pageToken)
                .setSingleEvents(true)
                .execute();
        nextSyncToken = eventsResponse.getNextSyncToken();
        if (eventsResponse.getItems().isEmpty()) {
          log.info("No upcoming events found.");
          break;
        }
        logResponse(eventsResponse);
        events.addAll(eventsResponse.getItems());
        pageToken = eventsResponse.getNextPageToken();
        log.info("Next sync token: {}", nextSyncToken);
        log.info("Next page token: {}", pageToken);
        log.info("Next page");
      } while (pageToken != null);
    } catch (IOException e) {
      log.error("An error occurred", e);
    }
    return new EventsResponse(events, nextSyncToken);
  }

  @NotNull
  private EventsResponse getEvents(UserId user, CalendarId calendarId, String syncToken) {
    final NetHttpTransport httpTransport;
    try {
      httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    } catch (GeneralSecurityException | IOException e) {
      return new EventsResponse(List.of(), "");
    }
    Optional<Credential> credentials = googleCredentialsService.getCredentials(user);
    if (credentials.isEmpty()) {
      return new EventsResponse(List.of(), "");
    }
    var service =
        new Calendar.Builder(httpTransport, JSON_FACTORY, credentials.orElseThrow()).build();
    List<Event> events = new ArrayList<>();
    String nextSyncToken = "";
    try {
      // Call the Calendar API
      log.info("Getting the updated events");
      String pageToken = null;
      do {
        var eventsResponse =
            service
                .events()
                .list(calendarId.id())
                .setPageToken(pageToken)
                .setSingleEvents(true)
                .setSyncToken(syncToken)
                .execute();
        nextSyncToken = eventsResponse.getNextSyncToken();

        if (eventsResponse.getItems().isEmpty()) {
          break;
        }
        logResponse(eventsResponse);
        events.addAll(eventsResponse.getItems());
        pageToken = eventsResponse.getNextPageToken();
        log.info("Next sync token: {}", nextSyncToken);
        log.info("Next page token: {}", pageToken);
        log.info("Next page");
      } while (pageToken != null);
    } catch (IOException e) {
      log.error("An error occurred", e);
    }
    return new EventsResponse(events, nextSyncToken);
  }

  private static void logResponse(Events eventsResponse) {
    eventsResponse
        .getItems()
        .forEach(
            event -> {
              DateTime startDateTime =
                  event.getStart() == null ? null : event.getStart().getDateTime();
              String startAsString = startDateTime == null ? null : startDateTime.toString();
              DateTime endDateTime = event.getEnd() == null ? null : event.getEnd().getDateTime();
              String endAsString = endDateTime == null ? null : endDateTime.toString();
              log.info(
                  "{} {} {} {}",
                  startAsString,
                  endAsString,
                  event.getSummary(),
                  event.getLocation() == null ? "None" : event.getLocation());
            });
  }

  private static ZonedDateTime getNextMonthFirstDay(int year, int month, ZonedDateTime dateFrom) {
    return dateFrom
        .withYear(month == Month.DECEMBER.getValue() ? year + 1 : year)
        .withMonth(month % Month.DECEMBER.getValue() + 1)
        .withDayOfMonth(1)
        .truncatedTo(ChronoUnit.DAYS);
  }

  private static ZonedDateTime getFirstDayOfMonth(int year, int month) {
    return ZonedDateTime.now()
        .withYear(year)
        .withMonth(month)
        .withDayOfMonth(1)
        .truncatedTo(ChronoUnit.DAYS);
  }

  private record EventsResponse(List<Event> items, String nextSyncToken) {}
}
