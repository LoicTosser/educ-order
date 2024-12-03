package com.osslot.educorder.infrastructure.repository;

import com.osslot.educorder.domain.user.model.User.UserId;
import com.osslot.educorder.domain.user.model.UserSettings.GoogleCalendarSettings.CalendarId;
import com.osslot.educorder.infrastructure.activities.repository.GoogleAgendaCalendarRepository;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GoogleAgendaCalendarRepositoryTest implements WithAssertions {

  @Autowired private GoogleAgendaCalendarRepository repository;

  @Test
  void findAllByMonth() {
    var activities = repository.fromCalendar(new UserId("1234"), new CalendarId("1234"), 2024, 4);

    assertThat(activities.activities()).isEmpty();
  }
}
