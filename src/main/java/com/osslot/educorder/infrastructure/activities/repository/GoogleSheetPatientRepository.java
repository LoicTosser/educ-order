package com.osslot.educorder.infrastructure.activities.repository;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.osslot.educorder.domain.activities.model.Institution;
import com.osslot.educorder.domain.activities.model.Patient;
import com.osslot.educorder.domain.activities.repository.PatientRepository;
import com.osslot.educorder.infrastructure.activities.service.GoogleDriveService;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GoogleSheetPatientRepository implements PatientRepository {

  private static final String PATIENT_RANGE_NAME = "Patients!A2:M1000";
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  private final Sheets service;
  private final GoogleDriveService googleDriveService;
  private final List<Patient> allPatients;

  public GoogleSheetPatientRepository(
      GoogleCredentials googleCredentials, GoogleDriveService googleDriveService)
      throws GeneralSecurityException, IOException {
    this.service =
        new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                googleCredentials.getCredentials(GoogleNetHttpTransport.newTrustedTransport()))
            .build();
    this.googleDriveService = googleDriveService;
    allPatients = initPatients();
  }

  @Override
  public List<Patient> findAll() {
    return allPatients;
  }

  @Override
  public Optional<Patient> findByFullName(String fullName) {
    return allPatients.stream().filter(patient -> patient.fullName().equals(fullName)).findFirst();
  }

  private List<Patient> initPatients() {
    ValueRange response;
    try {
      response =
          service
              .spreadsheets()
              .values()
              .get(googleDriveService.getFacturationSheetId().orElseThrow(), PATIENT_RANGE_NAME)
              .execute();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    List<List<Object>> values = response.getValues();
    if (values == null || values.isEmpty()) {
      log.info("No data found.");
      return Collections.emptyList();
    }
    return values.stream()
        .map(PatientMapper::fromRow)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }

  @UtilityClass
  public static class PatientMapper {

    public static Optional<Patient> fromRow(List<Object> row) {
      if (row == null || row.isEmpty() || row.get(0) == null || row.get(0).toString().isEmpty()) {
        return Optional.empty();
      }
      return Optional.of(
          new Patient(
              row.get(1).toString(),
              row.get(0).toString(),
              row.get(3).toString(),
              Institution.fromFrenchName(row.get(2).toString())));
    }
  }
}
