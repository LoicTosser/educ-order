package com.osslot.educorder.application;

import com.osslot.educorder.domain.model.Institution;
import com.osslot.educorder.domain.repository.AdiaphKilometersFilesRepository;
import com.osslot.educorder.domain.service.ActivityKilometersService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@AllArgsConstructor
public class CreateADIAPHKilometersFiles {

    private final ActivityKilometersService activityKilometersService;
    private final AdiaphKilometersFilesRepository adiaphKilometersFilesRepository;

    public void execute(int month, int year) {
        var activitiesKilometersPerPatient =
               activityKilometersService.getActivitiesKilometersPerPatientBy(month, year, Institution.ADIAPH);
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

}
