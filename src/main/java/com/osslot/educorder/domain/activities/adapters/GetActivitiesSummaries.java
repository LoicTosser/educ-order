package com.osslot.educorder.domain.activities.adapters;

import com.osslot.educorder.domain.activities.model.Activity;
import com.osslot.educorder.domain.activities.model.ActivitySummaries;
import com.osslot.educorder.domain.activities.repository.ActivityRepository;
import com.osslot.educorder.domain.model.User.UserId;
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
      UserId userId, ZonedDateTime start, ZonedDateTime end) {
    List<Activity> activities = fireStoreActivityRepository.findAllBetween(userId, start, end);
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
    return new ActivitySummaries(userId, start, end, activitiesByInstitutionsAndPatientsAndTypes);
  }
}
