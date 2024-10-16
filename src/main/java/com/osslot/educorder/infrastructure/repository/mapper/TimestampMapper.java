package com.osslot.educorder.infrastructure.repository.mapper;

import com.google.cloud.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TimestampMapper {

  public static Timestamp fromZonedDateTime(ZonedDateTime zonedDateTime) {
    var zonedDateTimeUTC = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC);
    return Timestamp.of(java.sql.Timestamp.from(zonedDateTimeUTC.toInstant()));
  }

  public static ZonedDateTime toZonedDateTime(Timestamp timestamp) {
    return ZonedDateTime.ofInstant(timestamp.toSqlTimestamp().toInstant(), ZoneOffset.UTC);
  }
}
