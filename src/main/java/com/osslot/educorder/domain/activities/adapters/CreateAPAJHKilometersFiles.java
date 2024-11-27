package com.osslot.educorder.domain.activities.adapters;

import com.osslot.educorder.domain.activities.model.Institution;
import com.osslot.educorder.domain.activities.repository.ApajhKilometersFilesRepository;
import com.osslot.educorder.domain.activities.service.ActivityKilometersService;
import com.osslot.educorder.domain.model.User;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class CreateAPAJHKilometersFiles {

  private final ActivityKilometersService activityKilometersService;
  private final ApajhKilometersFilesRepository apajhKilometersFilesRepository;

  public void execute(int year, int month) {
    var activitiesKilometersPerPatient =
        activityKilometersService.getActivitiesKilometersPerPatientBy(
            month, year, Institution.APAJH);
    activitiesKilometersPerPatient.forEach(
        (patient, activities) -> {
          var patientFileId =
              apajhKilometersFilesRepository.createPatientFilesFor(
                  year, month, patient, activities);
          if (patientFileId.isEmpty()) {
            log.warn("Patient file not created for " + patient.fullName());
          }
        });
  }

  public void execute(User user, ZonedDateTime start, ZonedDateTime end) {
    var activitiesKilometersPerPatient =
        activityKilometersService.getActivitiesKilometersPerPatientBetween(
            user.id(), start, end, Institution.APAJH);
    activitiesKilometersPerPatient.forEach(
        (patient, activities) -> {
          var patientFileId =
              apajhKilometersFilesRepository.createPatientFilesFor(start, end, patient, activities);
          if (patientFileId.isEmpty()) {
            log.warn("Patient file not created for " + patient.fullName());
          }
        });
  }
}
