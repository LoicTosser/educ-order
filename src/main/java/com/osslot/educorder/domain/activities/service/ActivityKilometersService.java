package com.osslot.educorder.domain.activities.service;

import com.osslot.educorder.domain.activities.model.Activity;
import com.osslot.educorder.domain.activities.model.Activity.ActivityType;
import com.osslot.educorder.domain.activities.model.ActivityKilometers;
import com.osslot.educorder.domain.activities.model.Institution;
import com.osslot.educorder.domain.activities.model.Location;
import com.osslot.educorder.domain.activities.model.RideDistance;
import com.osslot.educorder.domain.activities.repository.ActivityRepository;
import com.osslot.educorder.domain.activities.repository.RideDistanceRepository;
import com.osslot.educorder.domain.patient.model.Patient;
import com.osslot.educorder.domain.user.adapters.UserSettingsAdapter;
import com.osslot.educorder.domain.user.model.User.UserId;
import com.osslot.educorder.domain.user.model.UserSettings;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class ActivityKilometersService {

  public static final String HOME_LOCATION_NAME = "Domicile";
  private final ActivityRepository activityRepository;
  private final RideDistanceRepository rideDistanceRepository;
  private final UserSettingsAdapter userSettingsAdapter;
  private final Map<UserId, Location> homeLocations = new ConcurrentHashMap<>();
  private static final Set<ActivityType> activitiesWithKilometers =
      Set.of(ActivityType.CARE, ActivityType.MEETING, ActivityType.RESPITE_CARE);

  public ActivityKilometersService(
      ActivityRepository firestoreActivityRepository,
      RideDistanceRepository rideDistanceRepository,
      UserSettingsAdapter userSettingsAdapter) {
    this.activityRepository = firestoreActivityRepository;
    this.rideDistanceRepository = rideDistanceRepository;
    this.userSettingsAdapter = userSettingsAdapter;
  }

  public Map<Patient.PatientId, List<ActivityKilometers>> getActivitiesKilometersPerPatientBetween(
      UserId userId, ZonedDateTime start, ZonedDateTime end, Institution institution) {
    var activitiesKilometers = getActivitiesKilometersBy(userId, start, end, institution);
    return activitiesKilometers.collect(
        Collectors.toMap(
            activityKilometers -> activityKilometers.activity().patientId(),
            List::of,
            (list1, list2) -> Stream.concat(list1.stream(), list2.stream()).distinct().toList()));
  }

  public Stream<ActivityKilometers> getActivitiesKilometersBy(
      UserId userId, ZonedDateTime start, ZonedDateTime end, Institution institution) {
    var activities = activityRepository.findAllBetween(userId, start, end);
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
        .filter(activityKilometer -> activityKilometer.activity().institution().equals(institution))
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
                    Activity::institution,
                    toSortedList(Comparator.comparing(Activity::beginDate))));
    for (int i = 0; i < dailyActivities.size(); i++) {
      var previousActivity = i == 0 ? null : dailyActivities.get(i - 1);
      var nextActivity = i == dailyActivities.size() - 1 ? null : dailyActivities.get(i + 1);
      var activity = dailyActivities.get(i);
      var previousLocation = getPreviousLocation(activity, previousActivity);
      var nextLocation = getNextLocation(activity, nextActivity);
      var institutionDailyActivities = dailyActivitiesPerInstitution.get(activity.institution());
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
        || !activity.institution().equals(previousActivity.institution())) {
      return getHomeLocation(activity.userId());
    }
    return previousActivity.location();
  }

  private Location getNextLocation(Activity activity, Activity nextActivity) {
    if (nextActivity == null || !activity.institution().equals(nextActivity.institution())) {
      return getHomeLocation(activity.userId());
    }
    return nextActivity.location();
  }

  private @Nullable Location getHomeLocation(UserId userId) {
    return homeLocations.computeIfAbsent(
        userId,
        aUserId ->
            userSettingsAdapter
                .findByUserId(userId)
                .map(UserSettings::defaultLocation)
                .orElse(null));
  }

  private Long getDistanceTo(
      Activity activity, Location nextLocation, boolean lastActivityOfTheDay) {
    Long distanceBetween = getDistanceBetween(activity.location(), nextLocation);
    if (lastActivityOfTheDay && activity.institution().equals(Institution.ADIAPH)) {
      return Math.max(0, distanceBetween - 20);
    }
    return distanceBetween;
  }

  private Long getDistanceFrom(
      Location previousLocation, Activity activity, boolean firstActivityOfTheDay) {
    Long distanceBetween = getDistanceBetween(previousLocation, activity.location());
    if (firstActivityOfTheDay && activity.institution().equals(Institution.ADIAPH)) {
      return Math.max(0, distanceBetween - 20);
    }
    return distanceBetween;
  }

  private Long getDistanceBetween(Location from, Location to) {
    return rideDistanceRepository
        .findDistanceBetween(from, to)
        .map(RideDistance::getDistanceInKilometers)
        .orElse(0L);
  }
}
