package com.osslot.educorder.infrastructure.repository.mapper;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

@UtilityClass
public class EventDateTimeMapper {

    public static final DateTimeFormatter RFC_3339_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS]XXX")
                    .withResolverStyle(ResolverStyle.LENIENT);

    public static @NotNull ZonedDateTime toZonedDateTime(EventDateTime event) {
        return ZonedDateTime.parse(event.getDateTime().toStringRfc3339(), RFC_3339_FORMATTER);
    }

    public static @NotNull DateTime fromZonedDateTime(ZonedDateTime zonedDateTime) {
        return DateTime.parseRfc3339(zonedDateTime.format(RFC_3339_FORMATTER));
    }

}
