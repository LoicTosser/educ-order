package com.osslot.educorder.infrastructure.activities.repository.entity;

import static java.time.Duration.ofSeconds;

import com.osslot.educorder.domain.activities.model.Activity;
import com.osslot.educorder.domain.activities.model.Activity.ActivityStatus;
import com.osslot.educorder.domain.activities.model.Activity.ActivityType;
import com.osslot.educorder.domain.model.User.UserId;
import java.time.Instant;
import java.time.ZoneId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

@With
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ActivityEntity {

  private String id;
  private String userId;
  private String eventId;
  private PatientEntity patient;
  private Instant beginDate;
  private Instant endDate;
  private long durationInSeconds;
  private LocationEntity location;
  private ActivityType activityType;
  private ActivityStatus status;

  public static final String PATH = "activities";

  public static ActivityEntity fromDomain(Activity activity) {
    return new ActivityEntity(
        activity.id(),
        activity.userId().id(),
        activity.eventId(),
        PatientEntity.fromDomain(activity.patient()),
        activity.beginDate().toInstant(),
        activity.beginDate().plusSeconds(activity.duration().getSeconds()).toInstant(),
        activity.duration().getSeconds(),
        LocationEntity.fromDomain(activity.location()),
        activity.activityType(),
        activity.status());
  }

  public Activity toDomain() {
    return new Activity(
        id,
        new UserId(userId),
        eventId,
        patient.toDomain(),
        beginDate.atZone(ZoneId.of("UTC")),
        ofSeconds(durationInSeconds),
        location.toDomain(),
        activityType,
        status);
  }
}
