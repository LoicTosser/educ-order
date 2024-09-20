package com.osslot.educorder.application;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CreateAPAJHKilometersFilesTest implements WithAssertions {

  @Autowired private CreateAPAJHKilometersFiles createAPAJHKilometersFiles;

  @Test
  void execute() {
    // Given - When - Then
    createAPAJHKilometersFiles.execute(2024, 3);
  }
}
