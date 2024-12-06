package com.osslot.educorder.domain.patient.repository;

import com.osslot.educorder.domain.patient.model.Patient;
import java.util.Optional;

public interface LegacyPatientRepository {

  Optional<Patient> findByFullName(String fullName);
}
