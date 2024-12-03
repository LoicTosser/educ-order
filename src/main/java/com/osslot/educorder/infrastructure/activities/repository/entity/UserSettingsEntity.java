package com.osslot.educorder.infrastructure.activities.repository.entity;

import com.osslot.educorder.domain.user.model.User.UserId;
import com.osslot.educorder.domain.user.model.UserSettings;
import com.osslot.educorder.domain.user.model.UserSettings.GoogleCalendarSettings;
import com.osslot.educorder.domain.user.model.UserSettings.GoogleCalendarSettings.CalendarId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsEntity {

  public static final String PATH = "user_settings";

  private String userId;
  private GoogleCalendarSettingsEntity googleCalendarSettings;

  public UserSettings toDomain() {
    return new UserSettings(new UserId(getUserId()), getGoogleCalendarSettings().toDomain());
  }

  public static UserSettingsEntity fromDomain(UserSettings userSettings) {
    return new UserSettingsEntity(
        userSettings.userId().id(),
        new GoogleCalendarSettingsEntity(
            userSettings.googleCalendarSettings().calendarId().id(),
            userSettings.googleCalendarSettings().synchroEnabled()));
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class GoogleCalendarSettingsEntity {
    private String calendarId;
    private boolean synchroEnabled;

    public GoogleCalendarSettings toDomain() {
      return new GoogleCalendarSettings(new CalendarId(this.calendarId), this.synchroEnabled);
    }
  }
}
