package com.osslot.educorder.domain.repository;

import com.osslot.educorder.domain.model.Patient;
import java.util.List;
import java.util.Optional;

public interface PatientRepository {

  List<Patient> findAll();

  Optional<Patient> findByFullName(String fullName);
}
