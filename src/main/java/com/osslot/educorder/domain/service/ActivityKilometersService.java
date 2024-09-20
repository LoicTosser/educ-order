package com.osslot.educorder.domain.service;

import com.osslot.educorder.domain.model.Activity;
import com.osslot.educorder.domain.model.Activity.ActivityType;
import com.osslot.educorder.domain.model.ActivityKilometers;
import com.osslot.educorder.domain.model.Institution;
import com.osslot.educorder.domain.model.Location;
import com.osslot.educorder.domain.model.Patient;
import com.osslot.educorder.domain.repository.ActivityRepository;
import com.osslot.educorder.domain.repository.LocationRepository;
import com.osslot.educorder.domain.repository.RideDistanceRepository;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
public class ActivityKilometersService {

  public static final String HOME_LOCATION_NAME = "Domicile";
  private final ActivityRepository activityRepository;
  private final RideDistanceRepository rideDistanceRepository;
  private final Location homeLocation;
  private static final Set<ActivityType> activitiesWithKilometers =
      Set.of(ActivityType.CARE, ActivityType.MEETING, ActivityType.RESPITE_CARE);

  public ActivityKilometersService(
      ActivityRepository activityRepository,
      LocationRepository locationRepository,
      RideDistanceRepository rideDistanceRepository) {
    this.activityRepository = activityRepository;
    this.rideDistanceRepository = rideDistanceRepository;
    this.homeLocation = locationRepository.findByName(HOME_LOCATION_NAME).orElseThrow();
  }

  public Map<Patient, List<ActivityKilometers>> getActivitiesKilometersPerPatientBy(
      int month, int year, Institution institution) {
    var activitiesKilometers = getActivitiesKilometersBy(year, month, institution);
    return activitiesKilometers.collect(
        Collectors.toMap(
            activityKilometers -> activityKilometers.activity().patient(),
            List::of,
            (list1, list2) -> Stream.concat(list1.stream(), list2.stream()).distinct().toList()));
  }

  public Stream<ActivityKilometers> getActivitiesKilometersBy(
      int year, int month, Institution institution) {
    var activities = activityRepository.findAllByMonth(year, month);
    return getActivityKilometers(institution, activities);
  }

  public Map<Patient, List<ActivityKilometers>> getActivitiesKilometersPerPatientBetween(
      ZonedDateTime start, ZonedDateTime end, Institution institution) {
    var activitiesKilometers = getActivitiesKilometersBy(start, end, institution);
    return activitiesKilometers.collect(
        Collectors.toMap(
            activityKilometers -> activityKilometers.activity().patient(),
            List::of,
            (list1, list2) -> Stream.concat(list1.stream(), list2.stream()).distinct().toList()));
  }

  public Stream<ActivityKilometers> getActivitiesKilometersBy(
      ZonedDateTime start, ZonedDateTime end, Institution institution) {
    var activities = activityRepository.findAllBetween(start, end);
    return getActivityKilometers(institution, activities);
  }

  @NotNull
  private Stream<ActivityKilometers> getActivityKilometers(
      Institution institution, List<Activity> activities) {
    var institutionActivitiesPerDay =
        activities.stream()
            .filter(activity -> activitiesWithKilometers.contains(activity.activityType()))
            .collect(
                Collectors.groupingBy(
                    activity -> activity.beginDate().truncatedTo(ChronoUnit.DAYS),
                    toSortedList(Comparator.comparing(Activity::beginDate))));
    return institutionActivitiesPerDay.entrySet().stream()
        .flatMap(entry -> computeDailyActivitiesKilometersStream(entry.getValue()))
        .filter(
            activityKilometer ->
                activityKilometer.activity().patient().institution().equals(institution))
        .filter(activityKilometer -> activityKilometer.getTotalDistance() > 0)
        .sorted(
            Comparator.comparing(activityKilometers -> activityKilometers.activity().beginDate()));
  }

  private Stream<ActivityKilometers> computeDailyActivitiesKilometersStream(
      List<Activity> dailyActivities) {
    var dailyActivityKilometers = new ArrayList<ActivityKilometers>();
    var dailyActivitiesPerInstitution =
        dailyActivities.stream()
            .collect(
                Collectors.groupingBy(
                    activity -> activity.patient().institution(),
                    toSortedList(Comparator.comparing(Activity::beginDate))));
    for (int i = 0; i < dailyActivities.size(); i++) {
      var previousActivity = i == 0 ? null : dailyActivities.get(i - 1);
      var nextActivity = i == dailyActivities.size() - 1 ? null : dailyActivities.get(i + 1);
      var activity = dailyActivities.get(i);
      var previousLocation = getPreviousLocation(activity, previousActivity);
      var nextLocation = getNextLocation(activity, nextActivity);
      var institutionDailyActivities =
          dailyActivitiesPerInstitution.get(activity.patient().institution());
      var activityKilometers =
          new ActivityKilometers(
              activity,
              previousLocation,
              nextLocation,
              getDistanceFrom(
                  previousLocation, activity, institutionDailyActivities.indexOf(activity) == 0),
              getDistanceTo(
                  activity,
                  nextLocation,
                  institutionDailyActivities.indexOf(activity)
                      == institutionDailyActivities.size() - 1));
      dailyActivityKilometers.add(activityKilometers);
    }
    return dailyActivityKilometers.stream();
  }

  static <T> Collector<T, ?, List<T>> toSortedList(Comparator<? super T> c) {
    return Collectors.collectingAndThen(
        Collectors.toCollection(() -> new TreeSet<>(c)), ArrayList::new);
  }

  private Location getPreviousLocation(Activity activity, Activity previousActivity) {
    if (previousActivity == null
        || !activity.patient().institution().equals(previousActivity.patient().institution())) {
      return this.homeLocation;
    }
    return previousActivity.location();
  }

  private Location getNextLocation(Activity activity, Activity nextActivity) {
    if (nextActivity == null
        || !activity.patient().institution().equals(nextActivity.patient().institution())) {
      return this.homeLocation;
    }
    return nextActivity.location();
  }

  private Long getDistanceTo(
      Activity activity, Location nextLocation, boolean lastActivityOfTheDay) {
    Long distanceBetween = getDistanceBetween(activity.location(), nextLocation);
    if (lastActivityOfTheDay && activity.patient().institution().equals(Institution.ADIAPH)) {
      return Math.max(0, distanceBetween - 20);
    }
    return distanceBetween;
  }

  private Long getDistanceFrom(
      Location previousLocation, Activity activity, boolean firstActivityOfTheDay) {
    Long distanceBetween = getDistanceBetween(previousLocation, activity.location());
    if (firstActivityOfTheDay && activity.patient().institution().equals(Institution.ADIAPH)) {
      return Math.max(0, distanceBetween - 20);
    }
    return distanceBetween;
  }

  private Long getDistanceBetween(Location from, Location to) {
    return rideDistanceRepository.getDistanceInKilometers(from, to).orElse(0L);
  }
}
