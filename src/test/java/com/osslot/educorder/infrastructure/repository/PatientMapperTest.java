package com.osslot.educorder.infrastructure.repository;

import com.osslot.educorder.domain.activities.model.Institution;
import com.osslot.educorder.domain.activities.model.Patient;
import com.osslot.educorder.infrastructure.activities.repository.GoogleSheetPatientRepository.PatientMapper;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PatientMapperTest implements WithAssertions {

  @Test
  @DisplayName("should return empty optional when row is null")
  void fromRowWhenRowIsNull() {
    Optional<Patient> result = PatientMapper.fromRow(null);

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("should return empty optional when row is empty")
  void fromRowWhenRowIsEmpty() {
    Optional<Patient> result = PatientMapper.fromRow(Collections.emptyList());

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("should return patient when row is valid")
  void fromRowWhenRowIsValid() {
    List<Object> row = List.of("Doe", "John", "123456");

    Optional<Patient> result = PatientMapper.fromRow(row);

    assertThat(result).isNotEmpty();
    assertThat(result.get()).isEqualTo(new Patient("John", "Doe", "123456", Institution.ADIAPH));
  }
}
