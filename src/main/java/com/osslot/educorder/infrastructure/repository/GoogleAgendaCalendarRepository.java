package com.osslot.educorder.infrastructure.repository;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.osslot.educorder.domain.model.Activity;
import com.osslot.educorder.domain.model.Location;
import com.osslot.educorder.domain.model.Patient;
import com.osslot.educorder.domain.repository.CalendarRepository;
import com.osslot.educorder.domain.repository.LocationRepository;
import com.osslot.educorder.domain.repository.PatientRepository;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.Month;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GoogleAgendaCalendarRepository implements CalendarRepository {

  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  public static final String CALENDAR_ID =
      "b32341848b6870ac8899d82601c990e3146d29a36cc404a6df2bfc6aa893c9ae@group.calendar.google.com";
  public static final DateTimeFormatter RFC_3339_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS]XXX")
          .withResolverStyle(ResolverStyle.LENIENT);
  private final Calendar service;
  private final PatientRepository patientRepository;
  private final LocationRepository locationRepository;

  public GoogleAgendaCalendarRepository(
      GoogleCredentials googleCredentials,
      PatientRepository patientRepository,
      LocationRepository locationRepository)
      throws GeneralSecurityException, IOException {
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    this.service =
        new Calendar.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, googleCredentials.getCredentials(HTTP_TRANSPORT))
            .build();
    this.patientRepository = patientRepository;
    this.locationRepository = locationRepository;
  }

  @Override
  public List<Activity> fromCalendar(int year, int month) {
    var events = getEvents(year, month);
    var patients = patientRepository.findAll();
    var locations = locationRepository.findAll();
    return events.stream()
        .map(event -> fromEvent(event, patients, locations))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .sorted(Comparator.comparing(Activity::beginDate))
        .toList();
  }

  private List<Event> getEvents(int year, int month) {
    List<Event> monthEvents = new ArrayList<>();
    try {
      // Call the Calendar API
      var dateFrom = getFirstDayOfMonth(year, month);
      var dateTo = getNextMonthFirstDay(year, month, dateFrom);
      log.info("Getting the upcoming 10 events");
      String pageToken = null;
      do {
        var events =
            service
                .events()
                .list(CALENDAR_ID)
                .setTimeMin(new DateTime(RFC_3339_FORMATTER.format(dateFrom)))
                .setTimeMax(new DateTime(RFC_3339_FORMATTER.format(dateTo)))
                .setPageToken(pageToken)
                .setMaxResults(10)
                .setSingleEvents(true)
                .setOrderBy("startTime")
                .execute();
        if (events.getItems().isEmpty()) {
          log.info("No upcoming events found.");
          break;
        }
        events
            .getItems()
            .forEach(
                event -> {
                  String start = event.getStart().getDateTime().toString();
                  String end = event.getEnd().getDateTime().toString();
                  log.info(
                      start
                          + " "
                          + end
                          + " "
                          + event.getSummary()
                          + " "
                          + (event.getLocation() == null ? "None" : event.getLocation()));
                });
        monthEvents.addAll(events.getItems());
        pageToken = events.getNextPageToken();
        log.info("Next page");
      } while (pageToken != null);
    } catch (IOException e) {
      log.error("An error occurred", e);
    }
    return monthEvents;
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

  public Optional<Activity> fromEvent(
      Event event, List<Patient> patients, List<Location> locations) {
    log.info(
        String.format(
            "Event input: %s, %s, %s",
            event.getStart().getDateTime(),
            event.getSummary(),
            event.getLocation() != null ? event.getLocation() : "None"));

    var activityType = toActivityType(event);
    var patient = toPatient(event, patients);
    var location = patient.flatMap(p -> toLocation(event, p, locations));
    if (activityType.isEmpty() || patient.isEmpty() || location.isEmpty()) {
      return Optional.empty();
    }
    var activityStartDate =
        ZonedDateTime.parse(event.getStart().getDateTime().toStringRfc3339(), RFC_3339_FORMATTER);
    var activityEndDate =
        ZonedDateTime.parse(event.getEnd().getDateTime().toStringRfc3339(), RFC_3339_FORMATTER);
    var duration = Duration.between(activityStartDate, activityEndDate);
    var activity =
        new Activity(
            patient.get(), activityStartDate, duration, location.get(), activityType.get());
    log.info(
        String.format(
            "Activity: %s, %s, %s, %s, %s",
            activity.activityType().getFrenchName(),
            activity.beginDate(),
            activity.duration(),
            activity.patient().fullName(),
            activity.location().name()));
    return Optional.of(activity);
  }

  private Optional<Patient> toPatient(Event event, List<Patient> patients) {
    String eventName = event.getSummary().toLowerCase();
    return patients.stream()
        .filter(
            patient ->
                eventName.contains(patient.firstName().toLowerCase())
                    || eventName.contains(patient.lastName().toLowerCase()))
        .findFirst();
  }

  public Optional<Location> toLocation(Event event, Patient patient, List<Location> locations) {
    String eventLocation = event.getLocation();
    String eventName = event.getSummary().toLowerCase();
    Optional<Location> location = Optional.empty();
    if (eventLocation != null) {
      location =
          locations.stream().filter(l -> l.name().equalsIgnoreCase(eventLocation)).findFirst();
    }
    if (location.isEmpty()) {
      if (eventName.contains("visio")) {
        return getDomicile(locations);
      }
      return locations.stream().filter(l -> l.name().contains(patient.firstName())).findFirst();
    }
    return location;
  }

  public Optional<Location> getDomicile(List<Location> locations) {
    return locations.stream().filter(location -> location.name().equals("Domicile")).findFirst();
  }

  public Optional<Activity.ActivityType> toActivityType(Event event) {
    String eventName = event.getSummary().toLowerCase();
    return Arrays.stream(Activity.ActivityType.values())
        .filter(activityType -> eventName.contains(activityType.getFrenchName().toLowerCase()))
        .findFirst();
  }
}
