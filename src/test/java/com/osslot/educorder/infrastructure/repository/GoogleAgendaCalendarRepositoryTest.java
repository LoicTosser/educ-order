package com.osslot.educorder.infrastructure.repository;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GoogleAgendaCalendarRepositoryTest implements WithAssertions {

  @Autowired private GoogleAgendaCalendarRepository repository;

  @Test
  void findAllByMonth() {
    var activities = repository.fromCalendar(2024, 4);

    assertThat(activities).isNotEmpty();
  }
}
