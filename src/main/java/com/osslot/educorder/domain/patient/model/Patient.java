package com.osslot.educorder.domain.patient.model;

import com.osslot.educorder.domain.activities.model.Institution;
import com.osslot.educorder.domain.user.model.User.UserId;

public record Patient(
    PatientId id,
    UserId userId,
    String firstName,
    String lastName,
    String fullName,
    Institution institution) {

  public record PatientId(String id) {
    public static PatientId from(String id) {
      return new PatientId(id);
    }
  }
}
