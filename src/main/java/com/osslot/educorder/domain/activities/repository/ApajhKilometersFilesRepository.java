package com.osslot.educorder.domain.activities.repository;

import com.osslot.educorder.domain.activities.model.ActivityKilometers;
import com.osslot.educorder.domain.activities.model.Patient;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface ApajhKilometersFilesRepository {

  Optional<String> createPatientFilesFor(
      int year, int month, Patient patient, List<ActivityKilometers> activityKilometersList);

  Optional<String> createPatientFilesFor(
      ZonedDateTime start,
      ZonedDateTime end,
      Patient patient,
      List<ActivityKilometers> activityKilometersList);
}
