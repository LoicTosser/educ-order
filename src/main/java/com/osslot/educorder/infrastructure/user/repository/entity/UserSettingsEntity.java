package com.osslot.educorder.infrastructure.user.repository.entity;

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
  private static final String CURRENT_VERSION = "0.0.1";

  private String version;
  private String userId;
  private LocationEntity defaultLocation;
  private GoogleCalendarSettingsEntity googleCalendarSettings;

  public UserSettings toDomain() {
    return new UserSettings(
        new UserId(getUserId()),
        defaultLocation.toDomain(),
        getGoogleCalendarSettings().toDomain());
  }

  public static UserSettingsEntity fromDomain(UserSettings userSettings) {
    return new UserSettingsEntity(
        CURRENT_VERSION,
        userSettings.userId().id(),
        LocationEntity.fromDomain(userSettings.defaultLocation()),
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
