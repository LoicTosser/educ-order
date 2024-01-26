package com.osslot.educorder.infrastructure.repository;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GoogleSheetActivityRepositoryTest implements WithAssertions {

  @Autowired private GoogleSheetActivityRepository repository;

  @Test
  void findAllByMonth() {
    var activities = repository.findAllByMonth(2024, 1);

    assertThat(activities).isNotEmpty();
  }
}
