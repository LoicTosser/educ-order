package com.osslot.educorder.infrastructure.repository.entity;

import static java.time.Duration.*;

import com.google.cloud.Timestamp;
import com.osslot.educorder.domain.model.Activity;
import com.osslot.educorder.domain.model.Activity.ActivityStatus;
import com.osslot.educorder.domain.model.Activity.ActivityType;
import com.osslot.educorder.infrastructure.repository.entity.UserSettingsEntity.UserEntity;
import com.osslot.educorder.infrastructure.repository.mapper.TimestampMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

import java.time.Instant;
import java.time.ZoneId;

@With
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ActivityEntity {

  private String id;
  private UserEntity userEntity;
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
        UserEntity.fromDomain(activity.user()),
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
        userEntity.toDomain(),
        eventId,
        patient.toDomain(),
        beginDate.atZone(ZoneId.of("UTC")),
        ofSeconds(durationInSeconds),
        location.toDomain(),
        activityType,
        status);
  }
}
