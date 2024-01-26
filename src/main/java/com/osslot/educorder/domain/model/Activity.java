package com.osslot.educorder.domain.model;

import java.time.Duration;
import java.time.ZonedDateTime;

public record Activity(
    Patient patient,
    ZonedDateTime beginDate,
    Duration duration,
    Location location,
    ActivityType activityType) {

  public enum ActivityType {
    MEETING("Réunion"),
    PREPARATION("Préparation"),
    PROJECT("Synthèse et projet"),
    CARE("Intervention"),
    SUPERVISION("Supervision"),
    RESPITE_CARE("Séjour répit");

    private final String frenchName;

    ActivityType(String frenchName) {
      this.frenchName = frenchName;
    }

    public static ActivityType valueOfFrench(String frenchName) {
      for (ActivityType e : values()) {
        if (e.frenchName.equalsIgnoreCase(frenchName)) {
          return e;
        }
      }
      throw new IllegalArgumentException("Invalid french name " + frenchName);
    }

    public String getFrenchName() {
      return frenchName;
    }
  }
}
