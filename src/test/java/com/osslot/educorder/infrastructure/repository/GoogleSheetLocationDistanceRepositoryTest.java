package com.osslot.educorder.infrastructure.repository;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GoogleSheetLocationDistanceRepositoryTest implements WithAssertions {

  @Autowired private GoogleSheetLocationDistanceRepository repository;
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
  ;
}
