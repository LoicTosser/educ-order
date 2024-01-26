package com.osslot.educorder.infrastructure.repository;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.osslot.educorder.EducOrderApplication;
import com.osslot.educorder.domain.model.Location;
import com.osslot.educorder.domain.repository.LocationDistanceRepository;
import com.osslot.educorder.infrastructure.service.GoogleDriveService;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GoogleSheetLocationDistanceRepository implements LocationDistanceRepository {

  private static final String LOCATION_RANGE_NAME = "Lieux!E1:Z1000";
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  private final Sheets service;
  private final Map<String, Map<String, Long>> locationDistances;
  private final GoogleDriveService googleDriveService;

  public GoogleSheetLocationDistanceRepository(
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
    this.locationDistances = initLocationDistances();
  }

  private Map<String, Map<String, Long>> initLocationDistances() {
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
      return Map.of();
    }
    var toLocations =
        values.getFirst().stream()
            .filter(value -> value != null && !value.toString().isEmpty())
            .map(Object::toString)
            .toList();
    return values.stream()
        .skip(1)
        .filter(
            row ->
                row != null
                    && !row.isEmpty()
                    && row.getFirst() != null
                    && !row.getFirst().toString().isEmpty())
        .collect(
            Collectors.toMap(
                row -> row.getFirst().toString().strip(),
                row -> LocationDistancesMapper.fromRow(toLocations, row)));
  }

  @Override
  public Optional<Long> getDistanceInKilometers(Location from, Location to) {
    if (locationDistances.containsKey(from.name())
        && locationDistances.get(from.name()).containsKey(to.name())) {
      return Optional.of(locationDistances.get(from.name()).get(to.name()));
    }
    return Optional.empty();
  }

  @UtilityClass
  public static class LocationDistancesMapper {

    static Map<String, Long> fromRow(List<String> toLocations, List<Object> row) {
      if (row == null
          || row.isEmpty()
          || row.getFirst() == null
          || row.getFirst().toString().isEmpty()) {
        return Map.of();
      }
      return IntStream.range(0, toLocations.size())
          .mapToObj(i -> Map.entry(toLocations.get(i), Long.parseLong(row.get(i + 1).toString())))
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
  }
}
