package com.osslot.educorder.domain.service;

import com.osslot.educorder.domain.model.Institution;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ActivityKilometersServiceTest implements WithAssertions {

  @Autowired private ActivityKilometersService activityKilometersService;

  @Test
  void getActivitiesKilometersBy_whenValidMothAnYear_returnsActivitiesKilometers() {
    // Given
    var month = 1;
    var year = 2024;

    // When
    var activitiesKilometers =
        activityKilometersService.getActivitiesKilometersBy(month, year, Institution.APAJH);

    // Then
    assertThat(activitiesKilometers).isNotEmpty();
  }
}
