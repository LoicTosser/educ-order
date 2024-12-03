package com.osslot.educorder.infrastructure.activities.repository;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.osslot.educorder.application.EducOrderApplication;
import com.osslot.educorder.domain.activities.model.ActivityKilometers;
import com.osslot.educorder.domain.activities.repository.ApajhKilometersFilesRepository;
import com.osslot.educorder.domain.patient.model.Patient;
import com.osslot.educorder.infrastructure.activities.service.GoogleDriveService;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GoogleSheetApajhKilometersFilesRepository implements ApajhKilometersFilesRepository {

  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  private static final String RANGE_DATA = "data!A2:D2";
  private static final String APAJH_MONTH_KILOMETERS_RANGE = "kilomètres!A2:Z1000";
  private final Sheets service;
  private final GoogleDriveService googleDriveService;

  public GoogleSheetApajhKilometersFilesRepository(
      GoogleCredentials googleCredentials, GoogleDriveService googleDriveService)
      throws GeneralSecurityException, IOException {
    NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
    this.service =
        new Sheets.Builder(transport, JSON_FACTORY, googleCredentials.getCredentials(transport))
            .setApplicationName(EducOrderApplication.APPLICATION_NAME)
            .build();
    this.googleDriveService = googleDriveService;
  }

  @Override
  public Optional<String> createPatientFilesFor(
      int year, int month, Patient patient, List<ActivityKilometers> activityKilometersList) {
    return pushAphjhCharges(patient, month, year, activityKilometersList);
  }

  @Override
  public Optional<String> createPatientFilesFor(
      ZonedDateTime start,
      ZonedDateTime end,
      Patient patient,
      List<ActivityKilometers> activityKilometersList) {
    return pushAphjhCharges(patient, start, end, activityKilometersList);
  }

  public void initApajhMonthlyPatientFile(
      Patient patient, int month, int year, String patientFileId) {
    try {
      var valueRange = new ValueRange();
      valueRange.setValues(List.of(List.of(patient.lastName(), patient.firstName(), month, year)));
      valueRange.setRange(RANGE_DATA);
      var request =
          service
              .spreadsheets()
              .values()
              .update(patientFileId, RANGE_DATA, valueRange)
              .setValueInputOption("USER_ENTERED");
      var response = request.execute();
    } catch (IOException e) {
      System.out.println("An error occurred: " + e);
    }
  }

  public Optional<String> pushAphjhCharges(
      Patient patient, int month, int year, List<ActivityKilometers> aphjhCharges) {
    try {
      var patientFileId = this.googleDriveService.createApajhPatientFile(patient, month, year);
      if (patientFileId.isEmpty()) {
        return Optional.empty();
      }
      this.initApajhMonthlyPatientFile(patient, month, year, patientFileId.orElseThrow());
      pushAphjhCharges(aphjhCharges, patientFileId);
      return patientFileId;
    } catch (IOException e) {
      log.error("An error occurred", e);
      return Optional.empty();
    }
  }

  public Optional<String> pushAphjhCharges(
      Patient patient,
      ZonedDateTime start,
      ZonedDateTime end,
      List<ActivityKilometers> aphjhCharges) {
    try {
      var patientFileId = this.googleDriveService.createApajhPatientFile(patient, start, end);
      if (patientFileId.isEmpty()) {
        return Optional.empty();
      }
      this.initApajhMonthlyPatientFile(
          patient, start.getMonthValue(), start.getYear(), patientFileId.orElseThrow());
      pushAphjhCharges(aphjhCharges, patientFileId);
      return patientFileId;
    } catch (IOException e) {
      log.error("An error occurred", e);
      return Optional.empty();
    }
  }

  private void pushAphjhCharges(
      List<ActivityKilometers> aphjhCharges, Optional<String> patientFileId) throws IOException {
    String valueInputOption = "USER_ENTERED";
    List<List<Object>> values =
        aphjhCharges.stream()
            .map(
                activityKilometers -> {
                  List<Object> row =
                      List.of(
                          activityKilometers
                              .activity()
                              .beginDate()
                              .format(GoogleSheetActivityRepository.WRITE_DATE_TIME_FORMATTER),
                          activityKilometers.activity().location().address(),
                          activityKilometers.from().address(),
                          activityKilometers.to().address(),
                          activityKilometers.distanceFrom() + activityKilometers.distanceTo());
                  return row;
                })
            .toList();

    ValueRange body = new ValueRange().setValues(values);
    UpdateValuesResponse result =
        service
            .spreadsheets()
            .values()
            .update(patientFileId.orElseThrow(), APAJH_MONTH_KILOMETERS_RANGE, body)
            .setValueInputOption(valueInputOption)
            .execute();

    log.info(result.toString());
  }
}
