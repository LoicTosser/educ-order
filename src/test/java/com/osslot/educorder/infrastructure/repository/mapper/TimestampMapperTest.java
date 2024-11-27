package com.osslot.educorder.infrastructure.repository.mapper;

import com.google.cloud.Timestamp;
import com.osslot.educorder.infrastructure.activities.repository.mapper.TimestampMapper;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

class TimestampMapperTest implements WithAssertions {

  @Test
  void fromZonedDateTime_withValidZonedDateTime_shouldReturnTimestamp() {
    // Given
    var zonedDateTime = ZonedDateTime.now();

    // When
    var timestamp = TimestampMapper.fromZonedDateTime(zonedDateTime);

    // Then
    assertThat(timestamp).isNotNull();
    assertThat(timestamp.toSqlTimestamp().toInstant())
        .isEqualTo(zonedDateTime.withZoneSameInstant(ZoneOffset.UTC).toInstant());
  }

  @Test
  void toZonedDateTime_withValidTimestamp_shouldReturnZonedDateTime() {
    // Given
    var timestamp = Timestamp.now();

    // When
    var zonedDateTime = TimestampMapper.toZonedDateTime(timestamp);

    // Then
    assertThat(zonedDateTime).isNotNull();
    assertThat(zonedDateTime.toInstant()).isEqualTo(timestamp.toDate().toInstant());
  }
}
