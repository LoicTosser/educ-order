package com.osslot.educorder.domain.activities.model;

public record RideDistance(RideDistanceId rideDistanceId, Ride ride, long distanceInMeters) {

  public Long getDistanceInKilometers() {
    return Float.valueOf(Math.round(Long.valueOf(distanceInMeters).floatValue() / 1000))
        .longValue();
  }

  public record RideDistanceId(String id) {}

  public record Ride(Location from, Location to) {
    public Ride opposite() {
      return new Ride(to, from);
    }
  }
}
