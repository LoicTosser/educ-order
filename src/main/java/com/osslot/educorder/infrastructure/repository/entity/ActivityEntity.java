package com.osslot.educorder.infrastructure.repository.entity;

import com.google.cloud.Timestamp;
import com.osslot.educorder.domain.model.Activity;
import com.osslot.educorder.domain.model.Activity.ActivityStatus;
import com.osslot.educorder.domain.model.Activity.ActivityType;
import com.osslot.educorder.infrastructure.repository.mapper.TimestampMapper;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

import static java.time.Duration.*;

@With
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ActivityEntity {

  private String id;
  private String eventId;
  private PatientEntity patient;
  private Timestamp beginDate;
  private long durationInSeconds;
  private LocationEntity location;
  private ActivityType activityType;
  private ActivityStatus status;

  public static final String PATH = "tasks";

  public static ActivityEntity fromDomain(Activity activity) {
    return new ActivityEntity(
        activity.id(),
        activity.eventId(),
        PatientEntity.fromDomain(activity.patient()),
        TimestampMapper.fromZonedDateTime(activity.beginDate()),
        activity.duration().getSeconds(),
        LocationEntity.fromDomain(activity.location()),
        activity.activityType(),
        activity.status());
  }

  public Activity toDomain() {
    return new Activity(
        id,
        eventId,
        patient.toDomain(),
        TimestampMapper.toZonedDateTime(beginDate),
        ofSeconds(durationInSeconds),
        location.toDomain(),
        activityType,
        status);
  }
}
