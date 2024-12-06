package com.osslot.educorder.infrastructure.activities.legacy;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.osslot.educorder.application.EducOrderApplication;
import com.osslot.educorder.domain.activities.model.Location;
import com.osslot.educorder.domain.activities.model.RideDistance;
import com.osslot.educorder.domain.activities.model.RideDistance.Ride;
import com.osslot.educorder.infrastructure.activities.repository.GoogleCredentials;
import com.osslot.educorder.infrastructure.activities.service.GoogleDriveService;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GoogleSheetRideDistanceService {

  private static final String DISTANCES_RANGE_NAME = "Distances!A2:Z10000";
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  private final Sheets service;
  private final GoogleDriveService googleDriveService;

  public GoogleSheetRideDistanceService(
      GoogleCredentials googleCredentials, GoogleDriveService googleDriveService)
      throws GeneralSecurityException, IOException {
    this.service =
        new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                googleCredentials.getCredentials(GoogleNetHttpTransport.newTrustedTransport()))
            .setApplicationName(EducOrderApplication.APPLICATION_NAME)
            .build();
    this.googleDriveService = googleDriveService;
  }

  public List<RideDistance> findAllRideDistances() {
    ValueRange response;
    try {
      response =
          service
              .spreadsheets()
              .values()
              .get(googleDriveService.getFacturationSheetId().orElseThrow(), DISTANCES_RANGE_NAME)
              .execute();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    List<List<Object>> values = response.getValues();
    if (values == null || values.isEmpty()) {
      log.info("No data found.");
      return List.of();
    }
    return values.stream()
        .skip(1)
        .filter(
            row ->
                row != null
                    && !row.isEmpty()
                    && row.getFirst() != null
                    && !row.getFirst().toString().isEmpty())
        .flatMap(this::toRidesDistances)
        .toList();
  }

  private Stream<RideDistance> toRidesDistances(List<Object> row) {
    String fromAddress = row.getFirst().toString().strip();
    var fromLocation = new Location(fromAddress);
    String toAddress = row.get(1).toString().strip();
    var toLocation = new Location(toAddress);
    return Stream.of(
        new RideDistance(
            new RideDistance.RideDistanceId(UUID.randomUUID().toString()),
            new Ride(fromLocation, toLocation),
            Long.parseLong(row.get(2).toString()) * 1000),
        new RideDistance(
            new RideDistance.RideDistanceId(UUID.randomUUID().toString()),
            new Ride(toLocation, fromLocation),
            Long.parseLong(row.get(2).toString()) * 1000));
  }
}
