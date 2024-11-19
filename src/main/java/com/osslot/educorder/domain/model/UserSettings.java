package com.osslot.educorder.domain.model;

import com.osslot.educorder.domain.model.User.UserId;
import lombok.Builder;

@Builder
public record UserSettings(UserId userId, GoogleCalendarSettings googleCalendarSettings) {

  @Builder
  public record GoogleCalendarSettings(CalendarId calendarId, boolean synchroEnabled) {
    public record CalendarId(String id) {}
  }
}
