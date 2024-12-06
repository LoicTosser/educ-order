package com.osslot.educorder.domain.patient.repository;

import com.osslot.educorder.domain.patient.model.Patient;
import com.osslot.educorder.domain.patient.model.Patient.PatientId;
import com.osslot.educorder.domain.user.model.User.UserId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface PatientRepository {

  List<Patient> findAllByUserId(UserId userId);

  Map<PatientId, Patient> findAllByIds(UserId userId, Set<PatientId> ids);

  Optional<Patient> findById(UserId userId, PatientId patientId);
}
