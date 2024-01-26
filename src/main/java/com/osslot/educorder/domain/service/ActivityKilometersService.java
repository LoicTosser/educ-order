package com.osslot.educorder.domain.service;

import com.osslot.educorder.domain.model.Activity;
import com.osslot.educorder.domain.model.ActivityKilometers;
import com.osslot.educorder.domain.model.Institution;
import com.osslot.educorder.domain.model.Location;
import com.osslot.educorder.domain.model.Patient;
import com.osslot.educorder.domain.repository.ActivityRepository;
import com.osslot.educorder.domain.repository.LocationDistanceRepository;
import com.osslot.educorder.domain.repository.LocationRepository;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ActivityKilometersService {

  public static final String HOME__LOCATION_NAME = "Domicile";
  private ActivityRepository activityRepository;
  private LocationRepository locationRepository;
  private LocationDistanceRepository locationDistanceRepository;

  public Map<Patient, List<ActivityKilometers>> getActivitiesKilometersPerPatientBy(int month, int year, Institution institution) {
    var activitiesKilometers =
            getActivitiesKilometersBy(year, month, institution);
    return
            activitiesKilometers
                    .collect(
                            Collectors.toMap(
                                    activityKilometers -> activityKilometers.activity().patient(),
                                    List::of,
                                    (list1, list2) ->
                                            Stream.concat(list1.stream(), list2.stream()).distinct().toList()));
  }

  public Stream<ActivityKilometers> getActivitiesKilometersBy(
      int year, int month, Institution institution) {
    var activities = activityRepository.findAllByMonth(year, month);
    var institutionActivitiesPerDay =
        activities.stream()
            .filter(activity -> activity.patient().institution().equals(institution))
            .collect(
                Collectors.toMap(
                    activity -> activity.beginDate().truncatedTo(ChronoUnit.DAYS),
                    List::of,
                    (list1, list2) ->
                        Stream.concat(list1.stream(), list2.stream()).distinct().toList()));
    var homeLocation = locationRepository.findByName(HOME__LOCATION_NAME).orElseThrow();
    return institutionActivitiesPerDay.entrySet().stream()
        .flatMap(entry -> computeDailyActivitiesKilometersStream(entry.getValue(), homeLocation))
        .sorted(
            Comparator.comparing(activityKilometers -> activityKilometers.activity().beginDate()));
  }

  private Stream<ActivityKilometers> computeDailyActivitiesKilometersStream(
      List<Activity> dailyActivities, Location homeLocation) {
    var dailyActivityKilometers = new ArrayList<ActivityKilometers>();
    for (int i = 0; i < dailyActivities.size(); i++) {
      var previousActivity = i == 0 ? null : dailyActivities.get(i - 1);
      var previousLocation = previousActivity == null ? homeLocation : previousActivity.location();
      var nextActivity = i == dailyActivities.size() - 1 ? null : dailyActivities.get(i + 1);
      var nextLocation = nextActivity == null ? homeLocation : nextActivity.location();
      var activity = dailyActivities.get(i);
      var activityKilometers =
          new ActivityKilometers(
              activity,
              previousLocation,
              nextLocation,
                  getDistanceFrom(previousLocation, activity, i==0),
                  getDistanceTo(activity, nextLocation, i==dailyActivities.size()-1));
      dailyActivityKilometers.add(activityKilometers);
    }
    return dailyActivityKilometers.stream();
  }

  private Long getDistanceTo(Activity activity, Location nextLocation, boolean lastActivityOfTheDay) {
    Long distanceBetween = getDistanceBetween(activity.location(), nextLocation);
    if (lastActivityOfTheDay && activity.patient().institution().equals(Institution.ADIAPH)) {
      return Math.max(0, distanceBetween - 20);
    }
    return distanceBetween;
  }

  private Long getDistanceFrom(Location previousLocation, Activity activity, boolean firstActivityOfTheDay) {
    Long distanceBetween = getDistanceBetween(previousLocation, activity.location());
    if (firstActivityOfTheDay && activity.patient().institution().equals(Institution.ADIAPH)) {
      return Math.max(0, distanceBetween - 20);
    }
    return distanceBetween;
  }

  private Long getDistanceBetween(Location from, Location to) {
    return locationDistanceRepository
            .getDistanceInKilometers(from, to)
            .orElse(0L);
  }
}
