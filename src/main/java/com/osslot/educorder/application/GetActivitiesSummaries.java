package com.osslot.educorder.application;

import com.osslot.educorder.domain.model.Activity;
import com.osslot.educorder.domain.model.ActivitySummaries;
import com.osslot.educorder.domain.model.UserSettings.User;
import com.osslot.educorder.domain.repository.ActivityRepository;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GetActivitiesSummaries {

  private final ActivityRepository fireStoreActivityRepository;

  public ActivitySummaries getActivitiesSummaries(
      User user, ZonedDateTime start, ZonedDateTime end) {
    List<Activity> activities = fireStoreActivityRepository.findAllBetween(user, start, end);
    var activitiesByInstitutionsAndPatientsAndTypes =
        activities.stream()
            .collect(
                Collectors.groupingBy(
                    activity -> activity.patient().institution().getFrenchName(),
                    Collectors.collectingAndThen(
                        Collectors.groupingBy(
                            activity -> activity.patient().fullName(),
                            Collectors.collectingAndThen(
                                Collectors.groupingBy(
                                    Activity::activityType,
                                    Collectors.collectingAndThen(
                                        Collectors.toList(), ActivitySummaries.Activities::new)),
                                    ActivitySummaries.PatientActivities::new)),
                        ActivitySummaries.InstitutionActivities::new)));
    return new ActivitySummaries(user, start, end, activitiesByInstitutionsAndPatientsAndTypes);
  }
}
