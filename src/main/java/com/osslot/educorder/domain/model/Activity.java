package com.osslot.educorder.domain.model;

import java.time.Duration;
import java.time.ZonedDateTime;
import lombok.Getter;

public record Activity(
    String eventId,
    Patient patient,
    ZonedDateTime beginDate,
    Duration duration,
    Location location,
    ActivityType activityType) {

  public Activity(
      Patient patient,
      ZonedDateTime beginDate,
      Duration duration,
      Location location,
      ActivityType activityType) {
    this("", patient, beginDate, duration, location, activityType);
  }

  @Getter
  public enum ActivityType {
    MEETING("Réunion"),
    PREPARATION("Préparation"),
    PROJECT("Synthèse et projet"),
    CARE("Intervention"),
    SUPERVISION("Supervision"),
    RESPITE_CARE("Séjour répit"),
    ESS("ESS"),
    ;

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
  }
}
