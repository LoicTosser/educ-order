package com.osslot.educorder.infrastructure.patient.repository.entity;

import com.osslot.educorder.domain.activities.model.Institution;
import com.osslot.educorder.domain.patient.model.Patient;
import com.osslot.educorder.domain.patient.model.Patient.PatientId;
import com.osslot.educorder.domain.user.model.User.UserId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class PatientEntity {

  private static final String CURRENT_VERSION = "0.0.1";

  private String version;
  private String id;
  private String userId;
  private String firstName;
  private String lastName;
  private String fullName;
  private Institution institution;

  public static PatientEntity fromDomain(Patient patient) {
    return new PatientEntity(
        CURRENT_VERSION,
        patient.id().id(),
        patient.userId().id(),
        patient.firstName(),
        patient.lastName(),
        patient.fullName(),
        patient.institution());
  }

  public Patient toDomain() {
    return new Patient(
        PatientId.from(id), new UserId(userId), firstName, lastName, fullName, institution);
  }
}
