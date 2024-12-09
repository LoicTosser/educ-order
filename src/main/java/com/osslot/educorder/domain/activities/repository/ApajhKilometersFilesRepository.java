package com.osslot.educorder.domain.activities.repository;

import com.osslot.educorder.domain.activities.model.ActivityKilometers;
import com.osslot.educorder.domain.patient.model.Patient;
import com.osslot.educorder.domain.user.model.User;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface ApajhKilometersFilesRepository {

  Optional<String> createPatientFilesFor(
      User.UserId userId,
      ZonedDateTime start,
      ZonedDateTime end,
      Patient patient,
      List<ActivityKilometers> activityKilometersList);
}
