package com.osslot.educorder.infrastructure.activities.repository.entity;

import com.osslot.educorder.domain.activities.model.Institution;
import com.osslot.educorder.domain.activities.model.Patient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class PatientEntity {
  private String firstName;
  private String lastName;
  private String fullName;
  private Institution institution;

  public static PatientEntity fromDomain(Patient patient) {
    return new PatientEntity(
        patient.firstName(), patient.lastName(), patient.fullName(), patient.institution());
  }

  public Patient toDomain() {
    return new Patient(firstName, lastName, fullName, institution);
  }
}
