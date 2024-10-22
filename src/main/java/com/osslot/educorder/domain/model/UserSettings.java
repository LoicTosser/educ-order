package com.osslot.educorder.domain.model;

import lombok.Builder;

@Builder
public record UserSettings(User user, GoogleCalendarSettings googleCalendarSettings) {

  @Builder
  public record User(String id, String name, String email) {}

  @Builder
  public record GoogleCalendarSettings(String calendarId, boolean synchroEnabled) {}
}
