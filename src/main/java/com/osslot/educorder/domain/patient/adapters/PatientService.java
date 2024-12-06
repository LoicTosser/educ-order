package com.osslot.educorder.domain.patient.adapters;

import com.osslot.educorder.domain.patient.model.Patient;
import com.osslot.educorder.domain.patient.model.Patient.PatientId;
import com.osslot.educorder.domain.patient.repository.PatientRepository;
import com.osslot.educorder.domain.user.model.User.UserId;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PatientService {

  private final PatientRepository patientRepository;

  public Optional<Patient> findById(UserId userId, PatientId patientId) {
    return patientRepository.findById(userId, patientId);
  }

  public Map<PatientId, Patient> findAllByIds(UserId userId, Set<PatientId> patientIds) {
    return patientRepository.findAllByIds(userId, patientIds);
  }
}
