package com.osslot.educorder.domain.activities.adapters;

import com.osslot.educorder.domain.activities.model.Activity;
import com.osslot.educorder.domain.activities.model.ActivitySummaries;
import com.osslot.educorder.domain.activities.model.ActivitySummaries.InstitutionActivities;
import com.osslot.educorder.domain.activities.model.ActivitySummaries.PatientActivities;
import com.osslot.educorder.domain.activities.repository.ActivityRepository;
import com.osslot.educorder.domain.patient.adapters.PatientService;
import com.osslot.educorder.domain.patient.model.Patient;
import com.osslot.educorder.domain.user.model.User.UserId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GetActivitiesSummaries {

  private final ActivityRepository fireStoreActivityRepository;
  private final PatientService patientService;

  public ActivitySummaries getActivitiesSummaries(
      UserId userId, ZonedDateTime start, ZonedDateTime end) {
    List<Activity> activities = fireStoreActivityRepository.findAllBetween(userId, start, end);
    var patientIds = activities.stream().map(Activity::patientId).collect(Collectors.toSet());
    var patientsByIds = patientService.findAllByIds(userId, patientIds);
    var activitiesByInstitutionsAndPatientsAndTypes =
        activities.stream()
            .collect(
                Collectors.groupingBy(
                    GetActivitiesSummaries::getInstitutionFromActivity,
                    groupByPatient(patientsByIds)));
    return new ActivitySummaries(userId, start, end, activitiesByInstitutionsAndPatientsAndTypes);
  }

  private static @NotNull Collector<Activity, Object, InstitutionActivities>
      groupByPatient(Map<Patient.PatientId, Patient> patientsByIds) {
    return Collectors.collectingAndThen(
        Collectors.groupingBy(
            activity -> {
              var patient = patientsByIds.get(activity.patientId());
              return patient.fullName();
            },
            groupByActivityType(patientsByIds)),
        InstitutionActivities::new);
  }

  private static @NotNull Collector<Activity, Object, PatientActivities>
      groupByActivityType(Map<Patient.PatientId, Patient> patientsByIds) {
    return Collectors.collectingAndThen(
        Collectors.groupingBy(
            Activity::activityType,
            Collectors.collectingAndThen(Collectors.toList(), ActivitySummaries.Activities::new)),
        typeActivities -> {
          var patient =
              patientsByIds.get(
                  typeActivities.values().iterator().next().activities().getFirst().patientId());
          return new PatientActivities(patient, typeActivities);
        });
  }

  private static String getInstitutionFromActivity(Activity activity) {
    return activity.institution().getFrenchName();
  }
}
