package com.osslot.educorder.domain.model;

import com.osslot.educorder.domain.model.UserSettings.User;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.Getter;

public record Activity(
    String id,
    User user,
    String eventId,
    Patient patient,
    ZonedDateTime beginDate,
    Duration duration,
    Location location,
    ActivityType activityType,
    ActivityStatus status) {

  public Activity(
      User user,
      Patient patient,
      ZonedDateTime beginDate,
      Duration duration,
      Location location,
      ActivityType activityType) {
    this(
        UUID.randomUUID().toString(),
        user,
        UUID.randomUUID().toString(),
        patient,
        beginDate,
        duration,
        location,
        activityType,
        ActivityStatus.CONFIRMED);
  }

  public boolean isCancelled() {
    return status == ActivityStatus.CANCELLED;
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

  public enum ActivityStatus {
    CONFIRMED,
    CANCELLED,
    TENTATIVE
  }
}
