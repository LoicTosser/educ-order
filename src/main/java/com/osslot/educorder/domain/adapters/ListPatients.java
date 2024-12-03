package com.osslot.educorder.domain.adapters;

import com.osslot.educorder.domain.patient.model.Patient;
import com.osslot.educorder.domain.patient.repository.PatientRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ListPatients {

  private final PatientRepository patientRepository;

  public List<Patient> listPatients() {
    return patientRepository.findAll();
  }
}
