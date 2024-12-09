package com.osslot.educorder.domain.activities.adapters;

import com.osslot.educorder.domain.activities.model.ActivityKilometers;
import com.osslot.educorder.domain.activities.model.Institution;
import com.osslot.educorder.domain.activities.repository.AdiaphKilometersFilesRepository;
import com.osslot.educorder.domain.activities.repository.ApajhKilometersFilesRepository;
import com.osslot.educorder.domain.activities.service.ActivityKilometersService;
import com.osslot.educorder.domain.patient.adapters.PatientService;
import com.osslot.educorder.domain.patient.model.Patient;
import com.osslot.educorder.domain.user.model.User;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class CreateKilometersFiles {

  private final ActivityKilometersService activityKilometersService;
  private final AdiaphKilometersFilesRepository adiaphKilometersFilesRepository;
  private final ApajhKilometersFilesRepository apajhKilometersFilesRepository;
  private final PatientService patientService;

  public void createAphjhKilometersFiles(User user, ZonedDateTime start, ZonedDateTime end) {
    createKilometersFiles(user, start, end, this::createAphjhKilometersFiles);
  }

  public void createAdiaphKilometersFiles(User user, ZonedDateTime start, ZonedDateTime end) {
    createKilometersFiles(user, start, end, this::createAdiaphKilometersFiles);
  }

  private void createKilometersFiles(
      User user,
      ZonedDateTime start,
      ZonedDateTime end,
      Function<CreatePatientKilometersFilesRequest, Optional<String>>
          createPatientKilometersFiles) {
    var activitiesKilometersPerPatient =
        activityKilometersService.getActivitiesKilometersPerPatientBetween(
            user.id(), start, end, Institution.ADIAPH);
    activitiesKilometersPerPatient.forEach(
        (patientId, activities) -> {
          var patient = patientService.findById(user.id(), patientId);
          if (patient.isEmpty()) {
            log.warn("Patient not found for id {}", patientId);
            return;
          }
          var request =
              new CreatePatientKilometersFilesRequest(
                  user, start, end, patient.orElseThrow(), activities);
          var patientFileId = createPatientKilometersFiles.apply(request);
          if (patientFileId.isEmpty()) {
            log.warn("Patient file not created for {}", patient.orElseThrow().fullName());
          }
        });
  }

  private Optional<String> createAphjhKilometersFiles(
      CreatePatientKilometersFilesRequest createPatientKilometersFilesRequest) {
    return apajhKilometersFilesRepository.createPatientFilesFor(
        createPatientKilometersFilesRequest.user().id(),
        createPatientKilometersFilesRequest.start(),
        createPatientKilometersFilesRequest.end(),
        createPatientKilometersFilesRequest.patient(),
        createPatientKilometersFilesRequest.activities());
  }

  private Optional<String> createAdiaphKilometersFiles(
      CreatePatientKilometersFilesRequest createPatientKilometersFilesRequest) {
    return adiaphKilometersFilesRepository.createPatientFilesFor(
        createPatientKilometersFilesRequest.user().id(),
        createPatientKilometersFilesRequest.start(),
        createPatientKilometersFilesRequest.end(),
        createPatientKilometersFilesRequest.patient(),
        createPatientKilometersFilesRequest.activities());
  }

  private record CreatePatientKilometersFilesRequest(
      User user,
      ZonedDateTime start,
      ZonedDateTime end,
      Patient patient,
      List<ActivityKilometers> activities) {}
}
