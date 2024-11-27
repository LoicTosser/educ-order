package com.osslot.educorder.domain.activities.adapters;

import com.osslot.educorder.domain.activities.model.Institution;
import com.osslot.educorder.domain.activities.repository.AdiaphKilometersFilesRepository;
import com.osslot.educorder.domain.activities.service.ActivityKilometersService;
import com.osslot.educorder.domain.model.User;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class CreateADIAPHKilometersFiles {

  private final ActivityKilometersService activityKilometersService;
  private final AdiaphKilometersFilesRepository adiaphKilometersFilesRepository;

  public void execute(int year, int month) {
    var activitiesKilometersPerPatient =
        activityKilometersService.getActivitiesKilometersPerPatientBy(
            month, year, Institution.ADIAPH);
    activitiesKilometersPerPatient.forEach(
        (patient, activities) -> {
          var patientFileId =
              adiaphKilometersFilesRepository.createPatientFilesForMonth(
                  year, month, patient, activities);
          if (patientFileId.isEmpty()) {
            log.warn("Patient file not created for " + patient.fullName());
          }
        });
  }

  public void execute(User user, ZonedDateTime start, ZonedDateTime end) {
    var activitiesKilometersPerPatient =
        activityKilometersService.getActivitiesKilometersPerPatientBetween(
            user.id(), start, end, Institution.ADIAPH);
    activitiesKilometersPerPatient.forEach(
        (patient, activities) -> {
          var patientFileId =
              adiaphKilometersFilesRepository.createPatientFilesFor(
                  start, end, patient, activities);
          if (patientFileId.isEmpty()) {
            log.warn("Patient file not created for " + patient.fullName());
          }
        });
  }
}
