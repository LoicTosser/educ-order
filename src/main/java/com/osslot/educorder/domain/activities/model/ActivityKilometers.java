package com.osslot.educorder.domain.activities.model;

public record ActivityKilometers(
    Activity activity, Location from, Location to, Long distanceFrom, Long distanceTo) {

  public Long getTotalDistance() {
    return distanceFrom + distanceTo;
  }
}
