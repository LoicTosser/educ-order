package com.osslot.educorder.domain.adapters;

import com.osslot.educorder.domain.activities.model.Patient;
import com.osslot.educorder.domain.activities.repository.PatientRepository;
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
