package com.osslot.educorder.domain.patient.repository;

import com.osslot.educorder.domain.patient.model.Patient;
import com.osslot.educorder.domain.patient.model.Patient.PatientId;
import com.osslot.educorder.domain.user.model.User;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface PatientRepository {

  List<Patient> findAll();

  Optional<Patient> findByFullName(String fullName);

  Map<PatientId, Patient> findAllByIds(User.UserId userId, Set<PatientId> ids);

  Optional<Patient> findById(PatientId patientId);
}
