package com.osslot.educorder.application;

import com.osslot.educorder.domain.model.Institution;
import com.osslot.educorder.domain.model.UserSettings.User;
import com.osslot.educorder.domain.repository.ApajhKilometersFilesRepository;
import com.osslot.educorder.domain.service.ActivityKilometersService;
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
            user, start, end, Institution.APAJH);
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
