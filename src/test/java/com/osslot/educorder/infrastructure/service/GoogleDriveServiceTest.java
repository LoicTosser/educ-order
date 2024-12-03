package com.osslot.educorder.infrastructure.service;

import com.osslot.educorder.domain.activities.model.Institution;
import com.osslot.educorder.domain.patient.model.Patient;
import com.osslot.educorder.domain.user.model.User;
import com.osslot.educorder.infrastructure.activities.service.GoogleDriveService;
import java.util.UUID;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GoogleDriveServiceTest implements WithAssertions {

  @Autowired private GoogleDriveService googleDriveService;

  @Nested
  class GetMonthFolder {
    @Test
    void getMonthFolder_whenMonthIsDecember24_returnsEmpty() {
      // Given

      // When
      var monthFolder = googleDriveService.getMonthFolder(12, 2024);

      // Then
      assertThat(monthFolder).isEmpty();
    }

    @Test
    void getMonthFolder_whenMonthIsDecember23_returnsFolderId() {
      // Given

      // When
      var monthFolder = googleDriveService.getMonthFolder(12, 2023);

      // Then
      assertThat(monthFolder).isNotEmpty();
    }
  }

  @Nested
  class CreateMonthFolder {
    @Test
    void createMonthFolder_createsFolder() {
      // Given - When
      var monthFolder = googleDriveService.createMonthFolder(1, 2024);

      // Then
      assertThat(monthFolder).isNotEmpty();
    }
  }

  @Nested
  class CreateApajhPatientFile {
    @Test
    @DisplayName("Test Name")
    void when_unexistingFile_createsFile() {
      // Given
      var patient =
          new Patient(
              new Patient.PatientId(UUID.randomUUID().toString()),
              new User.UserId(UUID.randomUUID().toString()),
              "Toto",
              "Tata",
              "Toto tata",
              Institution.APAJH);

      // When
      var result = googleDriveService.createApajhPatientFile(patient, 1, 2024);

      // Then
      assertThat(result).isNotEmpty();
    }
  }
}
