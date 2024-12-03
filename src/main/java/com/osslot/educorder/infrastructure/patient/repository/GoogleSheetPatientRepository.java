package com.osslot.educorder.infrastructure.patient.repository;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.osslot.educorder.domain.activities.model.Institution;
import com.osslot.educorder.domain.patient.model.Patient;
import com.osslot.educorder.domain.patient.model.Patient.PatientId;
import com.osslot.educorder.domain.patient.repository.PatientRepository;
import com.osslot.educorder.domain.user.model.User;
import com.osslot.educorder.infrastructure.activities.repository.GoogleCredentials;
import com.osslot.educorder.infrastructure.activities.service.GoogleDriveService;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
  private final Map<String, Patient> patientsByFullName;
  private final Map<String, Patient> patientsByIds;

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
    var allPatients = initPatients();
    this.patientsByFullName =
        allPatients.stream().collect(Collectors.toMap(Patient::fullName, patient -> patient));
    this.patientsByIds =
        allPatients.stream()
            .collect(Collectors.toMap(patient -> patient.id().id(), patient -> patient));
  }

  @Override
  public List<Patient> findAll() {
    return new ArrayList<>(patientsByIds.values());
  }

  @Override
  public Optional<Patient> findByFullName(String fullName) {
    return Optional.ofNullable(patientsByFullName.get(fullName));
  }

  @Override
  public Map<PatientId, Patient> findAllByIds(User.UserId userId, Set<PatientId> ids) {
    return ids.stream()
        .map(PatientId::id)
        .map(patientsByIds::get)
        .filter(Objects::nonNull)
        .filter(patient -> patient.userId().equals(userId))
        .collect(Collectors.toMap(Patient::id, patient -> patient));
  }

  @Override
  public Optional<Patient> findById(PatientId patientId) {
    return Optional.ofNullable(patientsByIds.get(patientId.id()));
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
              new PatientId(row.get(0).toString()),
              new User.UserId(row.get(1).toString()),
              row.get(3).toString(),
              row.get(2).toString(),
              row.get(5).toString(),
              Institution.fromFrenchName(row.get(4).toString())));
    }
  }
}
