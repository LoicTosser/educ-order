package com.osslot.educorder.application;

import com.osslot.educorder.domain.model.Institution;
import com.osslot.educorder.domain.repository.ApajhKilometersFilesRepository;
import com.osslot.educorder.domain.service.ActivityKilometersService;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class CreateAPAJHKilometersFiles {

  private final ActivityKilometersService activityKilometersService;
  private final ApajhKilometersFilesRepository apajhKilometersFilesRepository;

  public void execute(int month, int year) {
      var activitiesKilometersPerPatient =
              activityKilometersService.getActivitiesKilometersPerPatientBy(month, year, Institution.APAJH);
    activitiesKilometersPerPatient.forEach(
        (patient, activities) -> {
          var patientFileId =
              apajhKilometersFilesRepository.createPatientFilesForMonth(
                  year, month, patient, activities);
          if (patientFileId.isEmpty()) {
            log.warn("Patient file not created for " + patient.fullName());
          }
        });
  }
}
