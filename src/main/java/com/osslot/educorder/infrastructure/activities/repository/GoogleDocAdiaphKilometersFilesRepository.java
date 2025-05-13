package com.osslot.educorder.infrastructure.activities.repository;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.model.BatchUpdateDocumentRequest;
import com.google.api.services.docs.v1.model.InsertTableRowRequest;
import com.google.api.services.docs.v1.model.InsertTextRequest;
import com.google.api.services.docs.v1.model.Location;
import com.google.api.services.docs.v1.model.ReplaceAllTextRequest;
import com.google.api.services.docs.v1.model.Request;
import com.google.api.services.docs.v1.model.SubstringMatchCriteria;
import com.google.api.services.docs.v1.model.TableCellLocation;
import com.osslot.educorder.application.EducOrderApplication;
import com.osslot.educorder.domain.activities.model.ActivityKilometers;
import com.osslot.educorder.domain.activities.repository.AdiaphKilometersFilesRepository;
import com.osslot.educorder.domain.patient.model.Patient;
import com.osslot.educorder.domain.user.model.User.UserId;
import com.osslot.educorder.infrastructure.activities.service.GoogleDriveService;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Month;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GoogleDocAdiaphKilometersFilesRepository implements AdiaphKilometersFilesRepository {

  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  private static final DateTimeFormatter WRITE_DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private final GoogleDriveService googleDriveService;
  private final GoogleCredentialsService googleCredentialsService;

  public GoogleDocAdiaphKilometersFilesRepository(
      GoogleCredentialsService googleCredentialsService, GoogleDriveService googleDriveService) {
    this.googleDriveService = googleDriveService;
    this.googleCredentialsService = googleCredentialsService;
  }

  @NotNull
  private Docs getDocs(UserId userId) {
    try {
      return new Docs.Builder(
              GoogleNetHttpTransport.newTrustedTransport(),
              JSON_FACTORY,
              googleCredentialsService
                  .getCredentials(userId)
                  .orElseThrow(() -> new RuntimeException("No credentials")))
          .setApplicationName(EducOrderApplication.APPLICATION_NAME)
          .build();
    } catch (GeneralSecurityException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Optional<String> createPatientFilesFor(
      UserId userId,
      ZonedDateTime start,
      ZonedDateTime end,
      Patient patient,
      List<ActivityKilometers> activityKilometersList) {
    var patientFileId = googleDriveService.createAdiaphPatientFile(userId, patient, start, end);
    if (patientFileId.isEmpty()) {
      return Optional.empty();
    }
    initPatientFile(
        userId,
        patientFileId.orElseThrow(),
        patient,
        start.getMonthValue(),
        start.getYear(),
        activityKilometersList.stream()
            .map(ActivityKilometers::getTotalDistance)
            .reduce(0L, Long::sum));
    updateRows(userId, patientFileId.orElseThrow(), activityKilometersList);
    return patientFileId;
  }

  private void initPatientFile(
      UserId userId,
      String patientFileId,
      Patient patient,
      int month,
      int year,
      Long totalDistance) {
    try {
      var requests =
          List.of(
              buildReplaceAllTextRequest("{{MOIS}}", monthAsString(month)),
              buildReplaceAllTextRequest("{{ANNEE}}", Integer.valueOf(year).toString()),
              buildReplaceAllTextRequest("{{NOM}}", patient.lastName()),
              buildReplaceAllTextRequest("{{PRENOM}}", patient.firstName()),
              buildReplaceAllTextRequest("{{TOTAL}}", totalDistance.toString()));
      var batchUpdateRequest = new BatchUpdateDocumentRequest().setRequests(requests);

      var result =
          getDocs(userId).documents().batchUpdate(patientFileId, batchUpdateRequest).execute();
      log.info(result.toString());
    } catch (IOException e) {

      throw new RuntimeException(e);
    }
  }

  private static Request buildReplaceAllTextRequest(String textToReplace, String replaceText) {
    return new Request()
        .setReplaceAllText(
            new ReplaceAllTextRequest()
                .setContainsText(
                    new SubstringMatchCriteria().setText(textToReplace).setMatchCase(true))
                .setReplaceText(replaceText));
  }

  private String monthAsString(int month) {
    return Month.of(month).getDisplayName(TextStyle.FULL, java.util.Locale.FRANCE);
  }

  private void updateRows(
      UserId userId, String patientFileId, List<ActivityKilometers> activityKilometersList) {
    insertMissingRows(userId, patientFileId, activityKilometersList.size());
    IntStream.range(0, activityKilometersList.size())
        .forEach(i -> updateRow(userId, patientFileId, i + 1, activityKilometersList.get(i)));
  }

  private void insertMissingRows(UserId userId, String patientFileId, int activitiesCount) {
    try {
      var getDocResult = getDocs(userId).documents().get(patientFileId).execute();
      var content = getDocResult.getBody().getContent();
      for (var row : content) {
        if (row.containsKey("table")) {
          var tableStartIndex = row.getStartIndex();
          var rowCount = row.getTable().getRows();
          if (activitiesCount <= rowCount) {
            return;
          }
          var insertRowRequest =
              IntStream.range(1, activitiesCount - rowCount + 2)
                  .mapToObj(
                      i ->
                          new Request()
                              .setInsertTableRow(
                                  new InsertTableRowRequest()
                                      .setTableCellLocation(
                                          new TableCellLocation()
                                              .setTableStartLocation(
                                                  new Location().setIndex(tableStartIndex))
                                              .setRowIndex(i)
                                              .setColumnIndex(1))
                                      .setInsertBelow(true)))
                  .toList();
          try {
            var result =
                getDocs(userId)
                    .documents()
                    .batchUpdate(
                        patientFileId,
                        new BatchUpdateDocumentRequest().setRequests(insertRowRequest))
                    .execute();
            log.info(result.toString());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          break;
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void updateRow(
      UserId userId, String patientFileId, int rowIndex, ActivityKilometers activityKilometers) {
    var text = activityKilometers.activity().beginDate().format(WRITE_DATE_TIME_FORMATTER);
    updateCell(userId, patientFileId, text, rowIndex, 0);
    var trip =
        activityKilometers.from().address()
            + " - "
            + activityKilometers.activity().location().address()
            + " - "
            + activityKilometers.to().address();
    var distances = activityKilometers.distanceFrom() + "\n\n" + activityKilometers.distanceTo();
    updateCell(userId, patientFileId, trip, rowIndex, 1);
    updateCell(userId, patientFileId, distances, rowIndex, 2);
    updateCell(
        userId,
        patientFileId,
        Long.valueOf(activityKilometers.distanceFrom() + activityKilometers.distanceTo())
            .toString(),
        rowIndex,
        3);
  }

  private void updateCell(
      UserId userId, String patientFileId, String text, int rowIndex, int cellIndex) {
    try {
      var getDocResult = getDocs(userId).documents().get(patientFileId).execute();
      var content = getDocResult.getBody().getContent();
      var tableRows =
          content.stream()
              .filter(row -> row.containsKey("table"))
              .findFirst()
              .orElseThrow()
              .getTable()
              .getTableRows();
      var tableCells = tableRows.get(rowIndex).getTableCells();
      var startIndex = tableCells.get(cellIndex).getContent().getFirst().getStartIndex();
      var requests = List.of(buildInsertTextRequest(text, startIndex));
      var result =
          getDocs(userId)
              .documents()
              .batchUpdate(patientFileId, new BatchUpdateDocumentRequest().setRequests(requests))
              .execute();
      log.info(result.toString());
      Thread.sleep(100);
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private Request buildInsertTextRequest(String text, int index) {
    return new Request()
        .setInsertText(
            new InsertTextRequest().setText(text).setLocation(new Location().setIndex(index)));
  }
}
