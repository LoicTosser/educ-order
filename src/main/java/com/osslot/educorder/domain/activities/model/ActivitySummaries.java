package com.osslot.educorder.domain.activities.model;

import com.osslot.educorder.domain.activities.model.Activity.ActivityType;
import com.osslot.educorder.domain.model.User.UserId;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public record ActivitySummaries(
    UserId userId,
    ZonedDateTime start,
    ZonedDateTime end,
    Map<String, InstitutionActivities> activitySummaries) {

  public record InstitutionActivities(
      Institution institution,
      Map<String, PatientActivities> patientActivities,
      Duration durations,
      int count) {

    public InstitutionActivities(Map<String, PatientActivities> patientActivities) {
      this(
          patientActivities.values().stream()
              .findFirst()
              .map(PatientActivities::patient)
              .map(Patient::institution)
              .orElseThrow(),
          patientActivities,
          totalDurations(
              patientActivities.values().stream()
                  .flatMap(
                      patientActivities1 -> patientActivities1.activitiesByType.values().stream())),
          getCount(
              patientActivities.values().stream()
                  .flatMap(
                      patientActivities1 ->
                          patientActivities1.activitiesByType.values().stream())));
    }
  }

  public record PatientActivities(
      Patient patient,
      Map<ActivityType, Activities> activitiesByType,
      Duration durations,
      int count) {

    public PatientActivities(Map<ActivityType, Activities> activitiesByType) {
      this(
          activitiesByType.values().stream()
              .findFirst()
              .map(Activities::activities)
              .map(List::getFirst)
              .map(Activity::patient)
              .orElseThrow(),
          activitiesByType,
          totalDurations(activitiesByType.values().stream()),
          getCount(activitiesByType.values().stream()));
    }
  }

  private static @NotNull Integer getCount(Stream<Activities> activitiesStream) {
    return activitiesStream.map(Activities::count).reduce(0, Integer::sum);
  }

  private static Duration totalDurations(Stream<Activities> activitiesStream) {
    return activitiesStream.map(Activities::durations).reduce(Duration.ZERO, Duration::plus);
  }

  public record Activities(List<Activity> activities, Duration durations, int count) {

    public Activities(List<Activity> activities) {
      this(
          activities,
          activities.stream().map(Activity::duration).reduce(Duration.ZERO, Duration::plus),
          activities.size());
    }

    public Duration durations() {
      return activities.stream().map(Activity::duration).reduce(Duration.ZERO, Duration::plus);
    }

    public int count() {
      return activities.size();
    }
  }
}
