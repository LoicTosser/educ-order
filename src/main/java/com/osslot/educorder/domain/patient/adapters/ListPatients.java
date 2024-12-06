package com.osslot.educorder.domain.patient.adapters;

import com.osslot.educorder.domain.patient.model.Patient;
import com.osslot.educorder.domain.patient.repository.PatientRepository;
import com.osslot.educorder.domain.user.model.User.UserId;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ListPatients {

  private final PatientRepository patientRepository;

  public List<Patient> listPatients(UserId userId) {
    return patientRepository.findAllByUserId(userId);
  }
}
