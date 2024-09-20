package com.osslot.educorder.infrastructure.repository;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.TravelMode;
import com.osslot.educorder.EducOrderApplication;
import com.osslot.educorder.domain.model.Location;
import com.osslot.educorder.domain.repository.LocationRepository;
import com.osslot.educorder.domain.repository.RideDistanceRepository;
import com.osslot.educorder.infrastructure.service.GoogleDriveService;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GoogleSheetRideDistanceRepository implements RideDistanceRepository {

  private static final String DISTANCES_RANGE_NAME = "Distances!A2:Z10000";
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  private final Sheets service;
  private final Map<Ride, Long> rideDistances;
  private final GoogleDriveService googleDriveService;
  private final LocationRepository locationRepository;
  private final String googleMapsApiKey;

  public GoogleSheetRideDistanceRepository(
      GoogleCredentials googleCredentials,
      GoogleDriveService googleDriveService,
      LocationRepository locationRepository,
      @Value("${google.maps.api.key}") String googleMapsApiKey)
      throws GeneralSecurityException, IOException {
    this.googleMapsApiKey = googleMapsApiKey;
    this.service =
        new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                googleCredentials.getCredentials(GoogleNetHttpTransport.newTrustedTransport()))
            .setApplicationName(EducOrderApplication.APPLICATION_NAME)
            .build();
    this.googleDriveService = googleDriveService;
    this.locationRepository = locationRepository;
    this.rideDistances = computeRideDistances();
  }

  private Map<Ride, Long> computeRideDistances() {
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
      return Map.of();
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
        .collect(
            Collectors.toMap(RideDistance::ride, RideDistance::distanceInKilometers, (a, b) -> a));
  }

  private Stream<RideDistance> toRidesDistances(List<Object> row) {
    String fromAddress = row.getFirst().toString().strip();
    var fromLocation =
        locationRepository
            .findByAddress(fromAddress)
            .orElseGet(() -> new Location("", fromAddress));
    String toAddress = row.get(1).toString().strip();
    var toLocation =
        locationRepository.findByAddress(toAddress).orElseGet(() -> new Location("", toAddress));
    return Stream.of(
        new RideDistance(new Ride(fromLocation, toLocation), Long.parseLong(row.get(2).toString())),
        new RideDistance(
            new Ride(toLocation, fromLocation), Long.parseLong(row.get(2).toString())));
  }

  @Override
  public Optional<Long> getDistanceInKilometers(Location from, Location to) {
    var ride = new Ride(from, to);
    return Optional.ofNullable(
        rideDistances.computeIfAbsent(
            ride,
            aRide ->
                computeDistanceAndStore(aRide)
                    .map(RideDistance::distanceInKilometers)
                    .orElse(null)));
  }

  Optional<RideDistance> computeDistanceAndStore(Ride ride) {
    var distance = computeRideDistance(ride);
    if (distance.isEmpty()) {
      return Optional.empty();
    }
    var locationDistance = new RideDistance(ride, distance.orElseThrow());
    persistLocationDistance(locationDistance);
    return Optional.of(locationDistance);
  }

  private void persistLocationDistance(RideDistance rideDistance) {
    var newRange = String.format("%s%d:1000", "Distances!A", 2 + rideDistances.size());
    log.info("New range {}", newRange);
    var valueRange = new ValueRange();
    valueRange.setValues(
        List.of(
            List.of(
                rideDistance.ride().from().address(),
                rideDistance.ride().to().address(),
                rideDistance.distanceInKilometers())));
    valueRange.setRange(newRange);
    try {
      var request =
          service
              .spreadsheets()
              .values()
              .update(
                  googleDriveService.getFacturationSheetId().orElseThrow(), newRange, valueRange);
      request.setValueInputOption("USER_ENTERED");
      var response = request.execute();
    } catch (IOException e) {
      log.error("Error while persisting location distance", e);
    }
  }

  private Optional<Long> computeRideDistance(Ride ride) {
    var context = new GeoApiContext.Builder().queryRateLimit(10).apiKey(googleMapsApiKey).build();
    try {
      var directions =
          DirectionsApi.getDirections(context, ride.from().address(), ride.to().address())
              .mode(TravelMode.DRIVING)
              .await();
      log.info(
          "Direction between ${} and ${} : ${}", ride.from(), ride.to(), directions.toString());
      if (directions.routes.length == 0) {
        return Optional.empty();
      }
      var distanceInMeters =
          Arrays.stream(directions.routes[0].legs)
              .mapToLong(leg -> leg.distance.inMeters)
              .reduce(Long::sum);
      return Optional.of(
          Float.valueOf(
                  Math.round(Long.valueOf(distanceInMeters.orElseThrow()).floatValue() / 1000))
              .longValue());
    } catch (ApiException | InterruptedException | IOException e) {
      log.error("Error While computing distance of ride " + ride, e);
      throw new RuntimeException(e);
    }
  }

  record RideDistance(Ride ride, long distanceInKilometers) {}

  record Ride(Location from, Location to) {}
}
