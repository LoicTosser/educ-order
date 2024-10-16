package com.osslot.educorder.infrastructure.repository;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.osslot.educorder.domain.model.Activity;
import com.osslot.educorder.domain.repository.CalendarRepository;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Month;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.osslot.educorder.infrastructure.repository.mapper.ActivityMapper;
import com.osslot.educorder.infrastructure.repository.mapper.EventDateTimeMapper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.context.LifecycleProperties;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GoogleAgendaCalendarRepository implements CalendarRepository {

  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  public static final String CALENDAR_ID =
      "b32341848b6870ac8899d82601c990e3146d29a36cc404a6df2bfc6aa893c9ae@group.calendar.google.com";
  private final Calendar service;
  private final ActivityMapper activityMapper;

  public GoogleAgendaCalendarRepository(
      GoogleCredentials googleCredentials,
      ActivityMapper activityMapper)
      throws GeneralSecurityException, IOException {
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    this.service =
        new Calendar.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, googleCredentials.getCredentials(HTTP_TRANSPORT))
            .build();
    this.activityMapper = activityMapper;
  }

  @Override
  public FetchCalendarActivitiesResponse fromCalendar(int year, int month) {
    var events = getEvents(year, month);
    return toFetchCalendarActivitiesResponse(events);
  }

  @Override
  public FetchCalendarActivitiesResponse fromCalendar(ZonedDateTime start, ZonedDateTime end) {
    var events = getEvents(start, end);
    return toFetchCalendarActivitiesResponse(events);
  }

  @Override
  public FetchCalendarActivitiesResponse fromLastSync(String syncToken) {
    var events = getEvents(syncToken);
    FetchCalendarActivitiesResponse allActivities = toFetchCalendarActivitiesResponse(events);
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
        events.nextSyncToken());
  }

  private @NotNull FetchCalendarActivitiesResponse toFetchCalendarActivitiesResponse(
      EventsResponse events) {
    return new FetchCalendarActivitiesResponse(
        events.items().stream()
            .map(activityMapper::fromEvent)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .sorted(Comparator.comparing(Activity::beginDate))
            .toList(),
        events.nextSyncToken());
  }

  private EventsResponse getEvents(int year, int month) {
    var dateFrom = getFirstDayOfMonth(year, month);
    var dateTo = getNextMonthFirstDay(year, month, dateFrom);
    return getEvents(dateFrom, dateTo);
  }

  @NotNull
  private EventsResponse getEvents(ZonedDateTime start, ZonedDateTime end) {
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
                .list(CALENDAR_ID)
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
  private EventsResponse getEvents(String syncToken) {
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
                .list(CALENDAR_ID)
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
