package com.osslot.educorder.infrastructure.repository;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.osslot.educorder.EducOrderApplication;
import com.osslot.educorder.domain.model.Location;
import com.osslot.educorder.domain.repository.LocationRepository;
import com.osslot.educorder.infrastructure.service.GoogleDriveService;
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
public class GoogleSheetLocationRepository implements LocationRepository {
  private static final String LOCATION_RANGE_NAME = "Lieux!A2:Z1000";
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  private final Sheets service;
  private final List<Location> allLocations;
  private final GoogleDriveService googleDriveService;

  public GoogleSheetLocationRepository(
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
    this.allLocations = initLocations();
  }

  @Override
  public List<Location> findAll() {
    return allLocations;
  }

  @Override
  public Optional<Location> findByName(String name) {
    return allLocations.stream().filter(location -> location.name().equals(name)).findFirst();
  }

  @Override
  public Optional<Location> findByAddress(String address) {
    return allLocations.stream().filter(location -> location.address().equals(address)).findFirst();
  }

  private List<Location> initLocations() {
    ValueRange response;
    try {
      response =
          service
              .spreadsheets()
              .values()
              .get(googleDriveService.getFacturationSheetId().orElseThrow(), LOCATION_RANGE_NAME)
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
        .map(LocationMapper::fromRow)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }

  @UtilityClass
  public static class LocationMapper {

    static Optional<Location> fromRow(List<Object> row) {
      if (row == null || row.isEmpty() || row.get(0) == null || row.get(0).toString().isEmpty()) {
        return Optional.empty();
      }
      return Optional.of(new Location(row.get(0).toString().strip(), row.get(1).toString()));
    }
  }
}
