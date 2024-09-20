package com.osslot.educorder.infrastructure.repository;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.osslot.educorder.EducOrderApplication;
import com.osslot.educorder.domain.model.Activity;
import com.osslot.educorder.domain.model.Location;
import com.osslot.educorder.domain.repository.ActivityRepository;
import com.osslot.educorder.domain.repository.LocationRepository;
import com.osslot.educorder.domain.repository.PatientRepository;
import com.osslot.educorder.infrastructure.service.GoogleDriveService;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GoogleSheetActivityRepository implements ActivityRepository {

  public static final DateTimeFormatter READ_DATE_TIME_FORMATTER =
      new DateTimeFormatterBuilder()
          .appendPattern("[dd/MM/yyyy HH:mm:ss]")
          .appendPattern("[dd/MM/yyyy HH:mm]")
          .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
          .toFormatter();

  public static final DateTimeFormatter WRITE_DATE_TIME_FORMATTER =
      new DateTimeFormatterBuilder().appendPattern("[dd/MM/yyyy HH:mm:ss]").toFormatter();
  private static final DateTimeFormatter READ_DURATION_FORMATTER =
      new DateTimeFormatterBuilder()
          .appendPattern("[h:mm:ss]")
          .appendPattern("[h:mm]")
          .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
          .toFormatter()
          .withLocale(Locale.FRENCH);

  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  private static final String ACTIVITY_RANGE_READ = "Prises en Charge!A2:10000";
  private static final String ACTIVITY_SHEET_NAME = "Prises en Charge!A";
  public static final ZoneId PARIS_ZONE_ID = ZoneId.of("Europe/Paris");
  private final Sheets service;
  private final GoogleDriveService googleDriveService;
  private final PatientRepository patientRepository;
  private final LocationRepository locationRepository;

  public GoogleSheetActivityRepository(
      GoogleCredentials googleCredentials,
      PatientRepository patientRepository,
      LocationRepository locationRepository,
      GoogleDriveService googleDriveService)
      throws GeneralSecurityException, IOException {
    this.patientRepository = patientRepository;
    this.locationRepository = locationRepository;
    this.service =
        new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                googleCredentials.getCredentials(GoogleNetHttpTransport.newTrustedTransport()))
            .setApplicationName(EducOrderApplication.APPLICATION_NAME)
            .build();
    this.googleDriveService = googleDriveService;
  }

  @Override
  public List<Activity> findAllByMonth(int year, int month) {
    return findAllFilteredBy(row -> matchMonth(month, year, row));
  }

  @Override
  public List<Activity> findAllBetween(ZonedDateTime start, ZonedDateTime end) {
    return findAllFilteredBy(row -> isBetween(row, start, end));
  }

  @NotNull
  private List<Activity> findAllFilteredBy(Predicate<List<Object>> rowFilter) {
    try {
      var response =
          service
              .spreadsheets()
              .values()
              .get(googleDriveService.getFacturationSheetId().orElseThrow(), ACTIVITY_RANGE_READ)
              .execute();
      List<List<Object>> values = response.getValues();
      if (values == null || values.isEmpty()) {
        return List.of();
      }
      return values.stream()
          .filter(
              row ->
                  !row.isEmpty()
                      && row.getFirst() != null
                      && !row.getFirst().toString().trim().isEmpty())
          .filter(rowFilter)
          .map(this::fromRow)
          .filter(Optional::isPresent)
          .map(Optional::orElseThrow)
          .toList();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<Activity> add(List<Activity> activities) {
    try {
      var existingValues =
          service
              .spreadsheets()
              .values()
              .get(googleDriveService.getFacturationSheetId().orElseThrow(), ACTIVITY_RANGE_READ)
              .execute()
              .getValues();
      var firstIndex =
          IntStream.range(0, existingValues.size())
              .dropWhile(
                  i ->
                      !existingValues.get(i).isEmpty()
                          && existingValues.get(i).getFirst() != null
                          && !existingValues.get(i).getFirst().toString().isEmpty())
              .findFirst()
              .orElseThrow();

      var newRange = String.format("%s%d:1000", ACTIVITY_SHEET_NAME, firstIndex + 2);
      log.info("New range {}", newRange);
      var rowsToInsert = activities.stream().map(GoogleSheetActivityRepository::toRow).toList();
      var valueRange = new ValueRange();
      valueRange.setValues(rowsToInsert);
      valueRange.setRange(newRange);
      var request =
          service
              .spreadsheets()
              .values()
              .update(
                  googleDriveService.getFacturationSheetId().orElseThrow(), newRange, valueRange);
      request.setValueInputOption("USER_ENTERED");
      // TODO : Map response to Activity list
      var response = request.execute();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return null;
  }

  private static boolean isBetween(List<Object> row, ZonedDateTime start, ZonedDateTime end) {
    var dateTimeAsStr = row.getFirst();
    if (dateTimeAsStr == null || dateTimeAsStr.toString().trim().isEmpty()) return false;
    var localDateTime =
        LocalDateTime.parse(dateTimeAsStr.toString().trim(), READ_DATE_TIME_FORMATTER);
    var zonedDateTime =
        ZonedDateTime.ofInstant(localDateTime.atZone(PARIS_ZONE_ID).toInstant(), PARIS_ZONE_ID);
    return zonedDateTime.isAfter(start) && zonedDateTime.isBefore(end);
  }

  private static boolean matchMonth(int month, int year, List<Object> row) {
    var dateTimeAsStr = row.getFirst();
    if (dateTimeAsStr == null || dateTimeAsStr.toString().trim().isEmpty()) return false;
    var localDate = LocalDate.parse(dateTimeAsStr.toString().trim(), READ_DATE_TIME_FORMATTER);
    return localDate.getMonth().getValue() == month && localDate.getYear() == year;
  }

  Optional<Activity> fromRow(List<Object> row) {
    var rowValue = row.stream().map(Object::toString).collect(Collectors.joining(", "));
    log.info(rowValue);
    var date =
        LocalDateTime.parse(
                row.get(0).toString(), READ_DATE_TIME_FORMATTER.withLocale(Locale.FRENCH))
            .atZone(PARIS_ZONE_ID);
    var durationTemporalAccessor = READ_DURATION_FORMATTER.parse(row.get(2).toString());
    var duration =
        Duration.ofHours(durationTemporalAccessor.get(ChronoField.HOUR_OF_AMPM))
            .plusMinutes(durationTemporalAccessor.get(ChronoField.MINUTE_OF_HOUR))
            .plusSeconds(durationTemporalAccessor.get(ChronoField.SECOND_OF_MINUTE));
    var patient = patientRepository.findByFullName(row.get(1).toString());
    Optional<Location> location = getLocation(row.get(4).toString());
    var activityType = Activity.ActivityType.valueOfFrench(row.get(3).toString());
    if (patient.isEmpty() || location.isEmpty()) {
      log.error("Patient or location not found for row {}", rowValue);
      return Optional.empty();
    }
    return Optional.of(
        new Activity(patient.orElseThrow(), date, duration, location.orElseThrow(), activityType));
  }

  private Optional<Location> getLocation(String locationValue) {
    if (locationValue == null || locationValue.isEmpty()) {
      return Optional.empty();
    }
    var location = locationRepository.findByAddress(locationValue);
    if (location.isPresent()) {
      return location;
    }
    location = locationRepository.findByName(locationValue);
    if (location.isPresent()) {
      return location;
    }
    return Optional.of(new Location("", locationValue));
  }

  static List<Object> toRow(Activity activity) {
    return List.of(
        activity.beginDate().format(WRITE_DATE_TIME_FORMATTER),
        activity.patient().fullName(),
        formatDuration(activity.duration()),
        activity.activityType().getFrenchName(),
        activity.location().address());
  }

  private static String formatDuration(Duration duration) {
    long seconds = duration.getSeconds();
    long absSeconds = Math.abs(seconds);
    String positive =
        String.format("%d:%02d:%02d", absSeconds / 3600, (absSeconds % 3600) / 60, absSeconds % 60);
    return seconds < 0 ? "-" + positive : positive;
  }
}
