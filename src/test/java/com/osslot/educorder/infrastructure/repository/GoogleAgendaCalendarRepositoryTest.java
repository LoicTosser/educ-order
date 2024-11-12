package com.osslot.educorder.infrastructure.repository;

import com.osslot.educorder.domain.model.UserSettings;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GoogleAgendaCalendarRepositoryTest implements WithAssertions {

  @Autowired private GoogleAgendaCalendarRepository repository;

  @Test
  void findAllByMonth() {
    var activities =
        repository.fromCalendar(new UserSettings.User("1234", "", ""), "1234", 2024, 4);

    assertThat(activities.activities()).isEmpty();
  }
}
