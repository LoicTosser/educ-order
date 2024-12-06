package com.osslot.educorder.domain.user.model;

import com.osslot.educorder.domain.activities.model.Location;
import com.osslot.educorder.domain.user.model.User.UserId;
import lombok.Builder;

@Builder
public record UserSettings(
    UserSettingsId userSettingsId,
    UserId userId,
    Location defaultLocation,
    GoogleCalendarSettings googleCalendarSettings) {

  @Builder
  public record GoogleCalendarSettings(CalendarId calendarId, boolean synchroEnabled) {
    public record CalendarId(String id) {}
  }

  @Builder
  public record UserSettingsId(String id) {}
}
