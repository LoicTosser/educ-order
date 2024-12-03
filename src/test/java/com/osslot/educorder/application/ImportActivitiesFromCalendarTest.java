package com.osslot.educorder.application;

import com.osslot.educorder.domain.activities.adapters.ImportActivitiesFromCalendar;
import com.osslot.educorder.domain.user.model.User;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ImportActivitiesFromCalendarTest implements WithAssertions {

  @Autowired private ImportActivitiesFromCalendar importActivitiesFromCalendar;

  @Test
  void importActivities_whenActivitiesInCalendar_WriteThemInGSheetFile() {
    importActivitiesFromCalendar.importActivities(new User.UserId("1234"), 2024, 3);
  }
}
