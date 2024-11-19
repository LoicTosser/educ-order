package com.osslot.educorder.infrastructure.repository;

import com.osslot.educorder.infrastructure.activities.repository.GoogleSheetLocationRepository;
import com.osslot.educorder.infrastructure.activities.repository.GoogleSheetRideDistanceRepository;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GoogleSheetRideDistanceRepositoryTest implements WithAssertions {

  @Autowired private GoogleSheetRideDistanceRepository repository;
  @Autowired private GoogleSheetLocationRepository locationRepository;

  @Test
  void getDistanceBetweenDomicileAndDomicileHosny_returns20() {
    // Given
    var domicile = locationRepository.findByName("Domicile").orElseThrow();
    var domicileHosny = locationRepository.findByName("Domicile Hosny").orElseThrow();

    // When
    var distance = repository.getDistanceInKilometers(domicile, domicileHosny);

    // Then
    assertThat(distance).contains(20L);
  }
}
