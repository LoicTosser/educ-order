package com.osslot.educorder.infrastructure.patient.repository.entity;

import com.osslot.educorder.domain.activities.model.Institution;
import com.osslot.educorder.domain.patient.model.Patient;
import com.osslot.educorder.domain.patient.model.Patient.PatientId;
import com.osslot.educorder.domain.user.model.User.UserId;
import com.osslot.educorder.infrastructure.common.repository.entity.MultiTenantEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class PatientEntity extends MultiTenantEntity {

  public static final String PATH = "patients";
  private static final String CURRENT_VERSION = "0.0.1";

  private String firstName;
  private String lastName;
  private String fullName;
  private Institution institution;

  public static PatientEntity fromDomain(Patient patient) {
    return PatientEntity.builder()
        .version(CURRENT_VERSION)
        .id(patient.id().id())
        .userId(patient.userId().id())
        .firstName(patient.firstName())
        .lastName(patient.lastName())
        .fullName(patient.fullName())
        .institution(patient.institution())
        .build();
  }

  public Patient toDomain() {
    return new Patient(
        PatientId.from(getId()),
        new UserId(getUserId()),
        firstName,
        lastName,
        fullName,
        institution);
  }
}
