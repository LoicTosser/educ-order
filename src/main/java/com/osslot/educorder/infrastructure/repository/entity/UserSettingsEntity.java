package com.osslot.educorder.infrastructure.repository.entity;

import com.osslot.educorder.domain.model.UserSettings;
import com.osslot.educorder.domain.model.UserSettings.GoogleCalendarSettings;
import com.osslot.educorder.domain.model.UserSettings.User;
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

  private UserEntity user;
  private GoogleCalendarSettingsEntity googleCalendarSettings;

  public UserSettings toDomain() {
    return new UserSettings(getUser().toDomain(), getGoogleCalendarSettings().toDomain());
  }

  public static UserSettingsEntity fromDomain(UserSettings userSettings) {
    return new UserSettingsEntity(
        UserEntity.fromDomain(userSettings.user()),
        new GoogleCalendarSettingsEntity(
            userSettings.googleCalendarSettings().calendarId(),
            userSettings.googleCalendarSettings().synchroEnabled()));
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UserEntity {
    private String id;
    private String name;
    private String email;

    public static UserEntity fromDomain(User user) {
      return new UserEntity(user.id(), user.name(), user.email());
    }

    public User toDomain() {
      return new User(this.id, this.name, this.email);
    }
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class GoogleCalendarSettingsEntity {
    private String calendarId;
    private boolean synchroEnabled;

    public GoogleCalendarSettings toDomain() {
      return new GoogleCalendarSettings(this.calendarId, this.synchroEnabled);
    }
  }
}
