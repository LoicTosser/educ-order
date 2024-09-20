package com.osslot.educorder.application;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CreateADIAPHKilometersFilesTest implements WithAssertions {

  @Autowired private CreateADIAPHKilometersFiles createADIAPHKilometersFiles;

  @Test
  void execute() {
    // Given - When - Then
    createADIAPHKilometersFiles.execute(2024, 1);
  }
}
