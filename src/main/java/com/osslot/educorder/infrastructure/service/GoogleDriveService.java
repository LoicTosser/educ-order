package com.osslot.educorder.infrastructure.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.osslot.educorder.domain.model.Patient;
import com.osslot.educorder.infrastructure.repository.GoogleCredentials;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GoogleDriveService {

  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  private static final Map<Integer, String> yearFolderIds = new HashMap<>();
  private final Drive driveService;
  private final String rootFolderId;
  private String facturationSheetId;
  private final Map<GoogleFileDescriptor, String> fileIds = new HashMap<>();

  public GoogleDriveService(
      GoogleCredentials googleCredentials,
      @Value("${google.drive.root-folder-id}") String rootFolderId)
      throws GeneralSecurityException, IOException {
    driveService =
        new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                googleCredentials.getCredentials(GoogleNetHttpTransport.newTrustedTransport()))
            .build();
    this.rootFolderId = rootFolderId;
  }

  public Optional<String> createMonthFolder(int month, int year) {
    log.info("Create month folder " + month);
    var yearFolderId = getYearFolderId(year);
    if (yearFolderId == null) {
      return Optional.empty();
    }
    var existingMonthFolderId = getFolderId(yearFolderId, String.valueOf(month));
    if (existingMonthFolderId.isPresent()) {
      return existingMonthFolderId;
    }
    return createFolder(Integer.valueOf(month).toString(), yearFolderId);
  }

  private Optional<String> createFolder(String folderName, String parentFolderId) {
    return createFile("application/vnd.google-apps.folder", folderName, parentFolderId);
  }

  private Optional<String> createFile(String mimeType, String folderName, String parentFolderId) {
    try {
      File fileMetadata = new File();
      fileMetadata.setName(String.valueOf(folderName));
      fileMetadata.setParents(Collections.singletonList(parentFolderId));
      fileMetadata.setMimeType(mimeType);
      File file = driveService.files().create(fileMetadata).execute();
      log.info("Created file : " + file.getId() + ", " + file.getName());
      return Optional.of(file.getId());
    } catch (IOException e) {
      log.error("An error occurred: ", e);
      return Optional.empty();
    }
  }

  public Optional<String> getFolderId(String name) {
    return getFolderId(rootFolderId, name);
  }

  public Optional<String> getFileIdFromCache(String mimeType, String parentFolderId, String name) {
    var googleFileDescriptor = new GoogleFileDescriptor(mimeType, parentFolderId, name);
    return Optional.ofNullable(
        fileIds.computeIfAbsent(
            googleFileDescriptor,
            descriptor ->
                getFiledId(descriptor.mimeType(), descriptor.parentFolderId(), descriptor.name())
                    .orElse(null)));
  }

  private Optional<String> getFiledId(String mimeType, String parentFolderId, String name) {
    String query =
        String.format(
            "mimeType = '%s' and '%s' in parents and name = '%s' and trashed = false",
            mimeType, parentFolderId, name);
    try {
      var results =
          driveService
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

  public Optional<String> getFolderId(String parentFolderId, String name) {
    return getFileIdFromCache("application/vnd.google-apps.folder", parentFolderId, name);
  }

  public Optional<String> getMonthFolder(int month, int year) {
    var yearFolderId = getYearFolderId(year);
    if (yearFolderId == null) {
      return Optional.empty();
    }
    return getFolderId(yearFolderId, String.valueOf(month));
  }

  private String getYearFolderId(int year) {
    return yearFolderIds.computeIfAbsent(
        year, aYear -> getFolderId(rootFolderId, aYear.toString()).orElse(null));
  }

  public Optional<String> getFacturationSheetId() {
    if (facturationSheetId == null) {
      facturationSheetId =
          getFolderId("2023")
              .flatMap(
                  folderId ->
                      getFileIdFromCache(
                          "application/vnd.google-apps.spreadsheet", folderId, "Compta 2023"))
              .orElse(null);
    }
    return Optional.ofNullable(facturationSheetId);
  }

  public Optional<String> getPatientFolder(Patient patient, int month, int year) {
    var monthFolderId = getMonthFolder(month, year);
    if (monthFolderId.isEmpty()) {
      return Optional.empty();
    }
    return getFolderId(monthFolderId.orElseThrow(), patient.firstName());
  }

  public Optional<String> createPatientFolder(Patient patient, int month, int year) {
    var patientFolderId = getPatientFolder(patient, month, year);
    if (patientFolderId.isPresent()) {
      return patientFolderId;
    }
    var monthFolderId = getMonthFolder(month, year);
    if (monthFolderId.isEmpty()) {
      monthFolderId = createMonthFolder(month, year);
    }
    return createFolder(patient.firstName(), monthFolderId.orElseThrow());
  }

  public Optional<String> getApajhPatientFileId(Patient patient, int month, int year) {
    var patientFolderId = getPatientFolder(patient, month, year);
    if (patientFolderId.isEmpty()) {
      return Optional.empty();
    }
    return getFileIdFromCache(
        "application/vnd.google-apps.spreadsheet",
        patientFolderId.orElseThrow(),
        String.format("Frais kilométriques %s %s-%d", patient.fullName(), month, year));
  }

  public Optional<String> createApajhPatientFile(Patient patient, int month, int year) {
    var patientFileId = getApajhPatientFileId(patient, month, year);
    if (patientFileId.isPresent()) {
      return patientFileId;
    }
    var patientFolderId = getPatientFolder(patient, month, year);
    if (patientFolderId.isEmpty()) {
      patientFolderId = createPatientFolder(patient, month, year);
    }
    var templateFileId =
        getFileIdFromCache(
            "application/vnd.google-apps.spreadsheet",
            getFolderId("Modèles").orElseThrow(),
            "Frais_kilo_APAJH_2023");
    if (templateFileId.isEmpty()) {
      return Optional.empty();
    }
    return copyFile(
        templateFileId.orElseThrow(),
        patientFolderId.orElseThrow(),
        String.format("Frais kilométriques %s %s-%d", patient.fullName(), month, year));
  }

  private Optional<String> getAdiaphPatientFileId(Patient patient, int month, int year) {
    var patientFolderId = getPatientFolder(patient, month, year);
    if (patientFolderId.isEmpty()) {
      return Optional.empty();
    }
    return getFileIdFromCache(
            "application/vnd.google-apps.document",
            patientFolderId.orElseThrow(),
            String.format("Feuille des frais kilométriques %s %s %d", patient.fullName(), month, year));
  }

  public Optional<String> createAdiaphPatientFile(Patient patient, int month, int year) {
    var patientFileId = getAdiaphPatientFileId(patient, month, year);
    if (patientFileId.isPresent()) {
      return patientFileId;
    }
    var patientFolderId = getPatientFolder(patient, month, year);
    if (patientFolderId.isEmpty()) {
      patientFolderId = createPatientFolder(patient, month, year);
    }
    var templateFileId =
            getFileIdFromCache(
                    "application/vnd.google-apps.document",
                    getFolderId("Modèles").orElseThrow(),
                    "Frais_kilo_ADIAPH");
    if (templateFileId.isEmpty()) {
      return Optional.empty();
    }
    return copyFile(
            templateFileId.orElseThrow(),
            patientFolderId.orElseThrow(),
            String.format("Feuille des frais kilométriques %s %s %d", patient.fullName(), month, year));
  }

  private Optional<String> copyFile(String fileId, String parentFolderId, String fileName) {
    try {
      var result =
          driveService
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

  //    def create_adiaph_patient_file(self, patient: Patient, month, patient_folder_id: str,
  // total_distance: int):
  //    creds = self.google_credentials.get_credentials()
  //            try:
  //    serviceDrive = build('drive', 'v3', credentials=creds)
  //    file_title = 'Feuille des frais kilométriques %s %s 2023' % (patient.full_name, month)
  //    results = serviceDrive.files().copy(fileId=ADIAPH_DOC_TEMPLATE_ID,
  //                                        body={"parents": [patient_folder_id], 'title':
  // file_title, 'name': file_title}).execute()
  //    print(results)
  //    patient_file_id = results['id']
  //
  //
  //    except HttpError as error:
  //    print(f'An error occurred: {error}')
  //
  //        try:
  //    service = build('docs', 'v1', credentials=creds)
  //
  //            # The ID of the spreadsheet to update.
  //            if patient_file_id == None:
  //            return
  //
  //            # Retrieve the documents contents from the Docs service.
  //    month_date = datetime.date(2023, month, 1)
  //            locale.setlocale(locale.LC_ALL, "")
  //    month_as_string = month_date.strftime("%B")
  //            locale.setlocale(locale.LC_ALL, locale.getdefaultlocale())
  //    requests = [
  //    {
  //        'replaceAllText': {
  //        'containsText': {
  //            'text': '{{MOIS}}',
  //                    'matchCase':  'true'
  //        },
  //        'replaceText': month_as_string,
  //    }}, {
  //        'replaceAllText': {
  //            'containsText': {
  //                'text': '{{ANNEE}}',
  //                        'matchCase':  'true'
  //            },
  //            'replaceText': '2023',
  //        }}, {
  //        'replaceAllText': {
  //            'containsText': {
  //                'text': '{{NOM}}',
  //                        'matchCase':  'true'
  //            },
  //            'replaceText': patient.last_name,
  //        }}, {
  //        'replaceAllText': {
  //            'containsText': {
  //                'text': '{{PRENOM}}',
  //                        'matchCase':  'true'
  //            },
  //            'replaceText': patient.first_name,
  //        }}, {
  //        'replaceAllText': {
  //            'containsText': {
  //                'text': '{{TOTAL}}',
  //                        'matchCase':  'true'
  //            },
  //            'replaceText': str(total_distance),
  //        }
  //    }
  //            ]
  //
  //    result = service.documents().batchUpdate(
  //            documentId=patient_file_id, body={'requests': requests}).execute()
  //
  //    print(result)
  //    except HttpError as error:
  //    print(f'An error occurred: {error}')
  //
  //        return patient_file_id

  static record GoogleFileDescriptor(String mimeType, String parentFolderId, String name) {}
}
