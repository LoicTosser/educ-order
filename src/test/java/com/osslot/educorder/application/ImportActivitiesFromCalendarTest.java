package com.osslot.educorder.application;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ImportActivitiesFromCalendarTest implements WithAssertions {

  @Autowired private ImportActivitiesFromCalendar importActivitiesFromCalendar;

  @Test
  void importActivities_whenActivitiesInCalendar_WriteThemInGSheetFile() {
    importActivitiesFromCalendar.importActivities(2024, 3);
  }
}
