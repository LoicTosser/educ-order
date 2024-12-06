package com.osslot.educorder.infrastructure.activities.repository.entity;

import static java.time.Duration.ofSeconds;

import com.osslot.educorder.domain.activities.model.Activity;
import com.osslot.educorder.domain.activities.model.Activity.ActivityStatus;
import com.osslot.educorder.domain.activities.model.Activity.ActivityType;
import com.osslot.educorder.domain.activities.model.Institution;
import com.osslot.educorder.domain.patient.model.Patient;
import com.osslot.educorder.domain.user.model.User.UserId;
import com.osslot.educorder.infrastructure.common.repository.entity.MultiTenantEntity;
import com.osslot.educorder.infrastructure.user.repository.entity.LocationEntity;
import java.time.Instant;
import java.time.ZoneId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import lombok.experimental.SuperBuilder;

@With
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ActivityEntity extends MultiTenantEntity {

  private static final String CURRENT_VERSION = "0.0.1";

  private String eventId;
  private String patientId;
  private Instant beginDate;
  private Instant endDate;
  private long durationInSeconds;
  private LocationEntity location;
  private String institution;
  private ActivityType activityType;
  private ActivityStatus status;

  public static final String PATH = "activities";

  public static ActivityEntity fromDomain(Activity activity) {
    return ActivityEntity.builder()
        .version(CURRENT_VERSION)
        .id(activity.id())
        .userId(activity.userId().id())
        .eventId(activity.eventId())
        .patientId(activity.patientId().id())
        .beginDate(activity.beginDate().toInstant())
        .endDate(activity.beginDate().plusSeconds(activity.duration().getSeconds()).toInstant())
        .durationInSeconds(activity.duration().getSeconds())
        .location(LocationEntity.fromDomain(activity.location()))
        .institution(activity.institution().name())
        .activityType(activity.activityType())
        .status(activity.status())
        .build();
  }

  public Activity toDomain() {
    return new Activity(
        getId(),
        new UserId(getUserId()),
        eventId,
        Patient.PatientId.from(patientId),
        beginDate.atZone(ZoneId.of("UTC")),
        ofSeconds(durationInSeconds),
        location.toDomain(),
        Institution.valueOf(institution),
        activityType,
        status);
  }
}
