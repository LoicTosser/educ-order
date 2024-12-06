package com.osslot.educorder.infrastructure.activities.service;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.TravelMode;
import com.osslot.educorder.domain.activities.model.RideDistance;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RideDistanceService {

  private final String googleMapsApiKey;

  public RideDistanceService(@Value("${google.maps.api.key}") String googleMapsApiKey) {
    this.googleMapsApiKey = googleMapsApiKey;
  }

  public Optional<RideDistance> calculateDistance(RideDistance.Ride ride) {
    if (ride.from().equals(ride.to())) {
      return Optional.of(
          new RideDistance(new RideDistance.RideDistanceId(UUID.randomUUID().toString()), ride, 0));
    }
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
          new RideDistance(
              new RideDistance.RideDistanceId(UUID.randomUUID().toString()),
              ride,
              distanceInMeters.orElseThrow()));
    } catch (ApiException | InterruptedException | IOException e) {
      log.error("Error While computing distance of ride {}", ride, e);
      return Optional.empty();
    }
  }
}
