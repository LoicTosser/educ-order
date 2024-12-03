package com.osslot.educorder.domain.patient.adapters;

import com.osslot.educorder.domain.patient.model.Patient;
import com.osslot.educorder.domain.patient.model.Patient.PatientId;
import com.osslot.educorder.domain.patient.repository.PatientRepository;
import com.osslot.educorder.domain.user.model.User;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PatientService {

  private final PatientRepository patientRepository;

  public Optional<Patient> findById(PatientId patientId) {
    return patientRepository.findById(patientId);
  }

  public Map<PatientId, Patient> findAllByIds(User.UserId userId, Set<PatientId> patientIds) {
    return patientRepository.findAllByIds(userId, patientIds);
  }
}
