package com.osslot.educorder.infrastructure.activities.legacy;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.osslot.educorder.domain.activities.model.Activity;
import com.osslot.educorder.domain.patient.model.Patient;
import com.osslot.educorder.domain.patient.repository.PatientRepository;
import com.osslot.educorder.infrastructure.activities.repository.GoogleCredentials;
import com.osslot.educorder.infrastructure.activities.service.GoogleDriveService;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GoogleSheetActivityExporter {

  public static final DateTimeFormatter WRITE_DATE_TIME_FORMATTER =
      new DateTimeFormatterBuilder().appendPattern("[dd/MM/yyyy HH:mm:ss]").toFormatter();

  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

  private final PatientRepository patientRepository;

  public GoogleSheetActivityExporter(
      GoogleCredentials googleCredentials,
      PatientRepository patientRepository,
      GoogleDriveService googleDriveService)
      throws GeneralSecurityException, IOException {
    this.patientRepository = patientRepository;
    //    this.service =
    //        new Sheets.Builder(
    //                GoogleNetHttpTransport.newTrustedTransport(),
    //                JSON_FACTORY,
    //
    // googleCredentials.getCredentials(GoogleNetHttpTransport.newTrustedTransport()))
    //            .setApplicationName(EducOrderApplication.APPLICATION_NAME)
    //            .build();
    //    this.googleDriveService = googleDriveService;
  }

  //  public List<Activity> add(List<Activity> activities) {
  //    try {
  //      var existingValues =
  //          service
  //              .spreadsheets()
  //              .values()
  //              .get(googleDriveService.getFacturationSheetId().orElseThrow(),
  // ACTIVITY_RANGE_READ)
  //              .execute()
  //              .getValues();
  //      var firstIndex =
  //          IntStream.range(0, existingValues.size())
  //              .dropWhile(
  //                  i ->
  //                      !existingValues.get(i).isEmpty()
  //                          && existingValues.get(i).getFirst() != null
  //                          && !existingValues.get(i).getFirst().toString().isEmpty())
  //              .findFirst()
  //              .orElseThrow();
  //
  //      var newRange = String.format("%s%d:1000", ACTIVITY_SHEET_NAME, firstIndex + 2);
  //      log.info("New range {}", newRange);
  //      var rowsToInsert = activities.stream().map(this::toRow).toList();
  //      var valueRange = new ValueRange();
  //      valueRange.setValues(rowsToInsert);
  //      valueRange.setRange(newRange);
  //      var request =
  //          service
  //              .spreadsheets()
  //              .values()
  //              .update(
  //                  googleDriveService.getFacturationSheetId().orElseThrow(), newRange,
  // valueRange);
  //      request.setValueInputOption("USER_ENTERED");
  //      // TODO : Map response to Activity list
  //      var response = request.execute();
  //    } catch (IOException e) {
  //      throw new RuntimeException(e);
  //    }
  //    return null;
  //  }

  List<Object> toRow(Activity activity) {
    var patient = patientRepository.findById(activity.userId(), activity.patientId());
    return List.of(
        activity.beginDate().format(WRITE_DATE_TIME_FORMATTER),
        patient.map(Patient::fullName).orElse(""),
        formatDuration(activity.duration()),
        activity.activityType().getFrenchName(),
        activity.location().address(),
        activity.eventId());
  }

  private static String formatDuration(Duration duration) {
    long seconds = duration.getSeconds();
    long absSeconds = Math.abs(seconds);
    String positive =
        String.format("%d:%02d:%02d", absSeconds / 3600, (absSeconds % 3600) / 60, absSeconds % 60);
    return seconds < 0 ? "-" + positive : positive;
  }
}
