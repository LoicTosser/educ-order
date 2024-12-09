package com.osslot.educorder.infrastructure.activities.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.osslot.educorder.domain.patient.model.Patient;
import com.osslot.educorder.domain.user.model.User.UserId;
import com.osslot.educorder.infrastructure.activities.repository.GoogleCredentials;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GoogleDriveService {

  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

  private static final java.time.format.DateTimeFormatter FRENCH_DATE_TIME_FORMATTER =
      DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT)
          .withLocale(Locale.FRENCH);
  private static final Map<Integer, String> yearFolderIds = new ConcurrentHashMap<>();
  public static final String APPLICATION_VND_GOOGLE_APPS_DOCUMENT =
      "application/vnd.google-apps.document";
  public static final String TEMPLATES_FOLDER_NAME = "Modèles";
  public static final String ADIAPH_KILOMETERS_TEMPLATE = "Frais_kilo_ADIAPH";
  private final String rootFolderId;
  private final Map<GoogleFileDescriptor, String> fileIds = new ConcurrentHashMap<>();
  private final GoogleCredentials googleCredentials;

  public GoogleDriveService(
      @Value("${google.drive.root-folder-id}") String rootFolderId,
      GoogleCredentials googleCredentials) {
    this.rootFolderId = rootFolderId;
    this.googleCredentials = googleCredentials;
  }

  private Drive getDriveService(UserId userId) {
    try {
      return new Drive.Builder(
              GoogleNetHttpTransport.newTrustedTransport(),
              JSON_FACTORY,
              googleCredentials
                  .getCredentials(userId)
                  .orElseThrow(() -> new IllegalStateException("No credentials")))
          .build();
    } catch (GeneralSecurityException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Optional<String> createMonthFolder(UserId userId, int month, int year) {
    log.info("Create month folder {}", month);
    var yearFolderId = getYearFolderId(userId, year);
    if (yearFolderId == null) {
      return Optional.empty();
    }
    var existingMonthFolderId = getFolderIdFromCache(userId, yearFolderId, String.valueOf(month));
    if (existingMonthFolderId.isPresent()) {
      return existingMonthFolderId;
    }
    return createFolder(userId, Integer.valueOf(month).toString(), yearFolderId);
  }

  private Optional<String> createFolder(UserId userId, String folderName, String parentFolderId) {
    return createFile(userId, "application/vnd.google-apps.folder", folderName, parentFolderId);
  }

  private Optional<String> createFile(
      UserId userId, String mimeType, String folderName, String parentFolderId) {
    try {
      File fileMetadata = new File();
      fileMetadata.setName(String.valueOf(folderName));
      fileMetadata.setParents(Collections.singletonList(parentFolderId));
      fileMetadata.setMimeType(mimeType);
      File file = getDriveService(userId).files().create(fileMetadata).execute();
      log.info("Created file : " + file.getId() + ", " + file.getName());
      return Optional.of(file.getId());
    } catch (IOException e) {
      log.error("An error occurred: ", e);
      return Optional.empty();
    }
  }

  public Optional<String> getFolderIdFromCache(UserId userId, String name) {
    return getFolderIdFromCache(userId, rootFolderId, name);
  }

  public Optional<String> getFileIdFromCache(
      UserId userId, String mimeType, String parentFolderId, String name) {
    var googleFileDescriptor = new GoogleFileDescriptor(mimeType, parentFolderId, name);
    return Optional.ofNullable(
        fileIds.computeIfAbsent(
            googleFileDescriptor,
            descriptor ->
                getFileId(
                        userId,
                        descriptor.mimeType(),
                        descriptor.parentFolderId(),
                        descriptor.name())
                    .orElse(null)));
  }

  private Optional<String> getFileId(
      UserId userId, String mimeType, String parentFolderId, String name) {
    String query =
        String.format(
            "mimeType = '%s' and '%s' in parents and name = '%s' and trashed = false",
            mimeType, parentFolderId, name);
    try {
      var results =
          getDriveService(userId)
              .files()
              .list()
              .setFields("nextPageToken, files(id, name, mimeType)")
              .setQ(query)
              .setPageSize(10)
              .execute();
      var items = results.get("files");
      if (items == null) {
        log.info("Folder not found.");
        return Optional.empty();
      }
      var foldersProperties = (List<Map<String, Object>>) items;
      if (foldersProperties.isEmpty()) {
        log.info("Folder not found.");
        return Optional.empty();
      }
      return Optional.of(foldersProperties.getFirst().get("id").toString());
    } catch (IOException e) {
      log.error("An error occured", e);
      return Optional.empty();
    }
  }

  public Optional<String> getFolderIdFromCache(UserId userId, String parentFolderId, String name) {
    return getFileIdFromCache(userId, "application/vnd.google-apps.folder", parentFolderId, name);
  }

  public Optional<String> getMonthFolder(UserId userId, int month, int year) {
    var yearFolderId = getYearFolderId(userId, year);
    if (yearFolderId == null) {
      return Optional.empty();
    }
    return getFolderIdFromCache(userId, yearFolderId, String.valueOf(month));
  }

  private String getYearFolderId(UserId userId, int year) {
    return yearFolderIds.computeIfAbsent(
        year, aYear -> getFolderIdFromCache(userId, rootFolderId, aYear.toString()).orElse(null));
  }

  public Optional<String> getPatientFolder(UserId userId, Patient patient, int month, int year) {
    var monthFolderId = getMonthFolder(userId, month, year);
    if (monthFolderId.isEmpty()) {
      return Optional.empty();
    }
    return getFolderIdFromCache(userId, monthFolderId.orElseThrow(), patient.firstName());
  }

  public Optional<String> createPatientFolder(UserId userId, Patient patient, int month, int year) {
    var patientFolderId = getPatientFolder(userId, patient, month, year);
    if (patientFolderId.isPresent()) {
      return patientFolderId;
    }
    var monthFolderId = getMonthFolder(userId, month, year);
    if (monthFolderId.isEmpty()) {
      monthFolderId = createMonthFolder(userId, month, year);
    }
    return createFolder(userId, patient.firstName(), monthFolderId.orElseThrow());
  }

  public Optional<String> getApajhPatientFileId(
      UserId userId, Patient patient, ZonedDateTime start, ZonedDateTime end) {
    var patientFolderId = getPatientFolder(userId, patient, start.getMonthValue(), start.getYear());
    if (patientFolderId.isEmpty()) {
      return Optional.empty();
    }
    return getFileIdFromCache(
        userId,
        "application/vnd.google-apps.spreadsheet",
        patientFolderId.orElseThrow(),
        String.format(
            "Frais kilométriques %s %s-%s",
            patient.fullName(),
            FRENCH_DATE_TIME_FORMATTER.format(start),
            FRENCH_DATE_TIME_FORMATTER.format(end)));
  }

  public Optional<String> createApajhPatientFile(
      UserId userId, Patient patient, ZonedDateTime start, ZonedDateTime end) {
    var patientFileId = getApajhPatientFileId(userId, patient, start, end);
    if (patientFileId.isPresent()) {
      return patientFileId;
    }
    var patientFolderId = getPatientFolder(userId, patient, start.getMonthValue(), start.getYear());
    if (patientFolderId.isEmpty()) {
      patientFolderId =
          createPatientFolder(userId, patient, start.getMonthValue(), start.getYear());
    }
    var templateFileId =
        getFileIdFromCache(
            userId,
            "application/vnd.google-apps.spreadsheet",
            getFolderIdFromCache(userId, TEMPLATES_FOLDER_NAME).orElseThrow(),
            "Frais_kilo_APAJH_2023");
    if (templateFileId.isEmpty()) {
      return Optional.empty();
    }
    return copyFile(
        userId,
        templateFileId.orElseThrow(),
        patientFolderId.orElseThrow(),
        String.format(
            "Frais kilométriques %s %s-%s",
            patient.fullName(),
            FRENCH_DATE_TIME_FORMATTER.format(start),
            FRENCH_DATE_TIME_FORMATTER.format(end)));
  }

  private Optional<String> getAdiaphPatientFileId(
      UserId userId, Patient patient, ZonedDateTime start, ZonedDateTime end) {
    int month = start.getMonthValue();
    int year = start.getYear();
    var patientFolderId = getPatientFolder(userId, patient, month, year);
    if (patientFolderId.isEmpty()) {
      return Optional.empty();
    }
    return getFileIdFromCache(
        userId,
        APPLICATION_VND_GOOGLE_APPS_DOCUMENT,
        patientFolderId.orElseThrow(),
        String.format(
            "Feuille des frais kilométriques %s %s %s",
            patient.fullName(),
            FRENCH_DATE_TIME_FORMATTER.format(start),
            FRENCH_DATE_TIME_FORMATTER.format(end)));
  }

  public Optional<String> createAdiaphPatientFile(
      UserId userId, Patient patient, ZonedDateTime start, ZonedDateTime end) {
    var patientFileId = getAdiaphPatientFileId(userId, patient, start, end);
    if (patientFileId.isPresent()) {
      return patientFileId;
    }
    int month = start.getMonthValue();
    int year = start.getYear();
    var patientFolderId = getPatientFolder(userId, patient, month, year);
    if (patientFolderId.isEmpty()) {
      patientFolderId = createPatientFolder(userId, patient, month, year);
    }
    var templateFileId =
        getFileIdFromCache(
            userId,
            APPLICATION_VND_GOOGLE_APPS_DOCUMENT,
            getFolderIdFromCache(userId, TEMPLATES_FOLDER_NAME).orElseThrow(),
            ADIAPH_KILOMETERS_TEMPLATE);
    if (templateFileId.isEmpty()) {
      return Optional.empty();
    }
    return copyFile(
        userId,
        templateFileId.orElseThrow(),
        patientFolderId.orElseThrow(),
        String.format(
            "Feuille des frais kilométriques %s %s %s",
            patient.fullName(),
            FRENCH_DATE_TIME_FORMATTER.format(start),
            FRENCH_DATE_TIME_FORMATTER.format(end)));
  }

  private Optional<String> copyFile(
      UserId userId, String fileId, String parentFolderId, String fileName) {
    try {
      var result =
          getDriveService(userId)
              .files()
              .copy(
                  fileId,
                  new File()
                      .setParents(Collections.singletonList(parentFolderId))
                      .setName(fileName))
              .execute();
      return Optional.of(result.getId());
    } catch (IOException e) {
      log.error("An error occured", e);
      return Optional.empty();
    }
  }

  record GoogleFileDescriptor(String mimeType, String parentFolderId, String name) {}
}
