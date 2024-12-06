package com.osslot.educorder.infrastructure.user.repository.entity;

import com.osslot.educorder.domain.user.model.User.UserId;
import com.osslot.educorder.domain.user.model.UserSettings;
import com.osslot.educorder.domain.user.model.UserSettings.GoogleCalendarSettings;
import com.osslot.educorder.domain.user.model.UserSettings.GoogleCalendarSettings.CalendarId;
import com.osslot.educorder.infrastructure.common.repository.entity.MultiTenantEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserSettingsEntity extends MultiTenantEntity {

  public static final String PATH = "user_settings";
  private static final String CURRENT_VERSION = "0.0.1";

  private LocationEntity defaultLocation;
  private GoogleCalendarSettingsEntity googleCalendarSettings;

  public UserSettings toDomain() {
    return UserSettings.builder()
        .userSettingsId(UserSettings.UserSettingsId.builder().id(getId()).build())
        .userId(new UserId(getUserId()))
        .defaultLocation(defaultLocation.toDomain())
        .googleCalendarSettings(googleCalendarSettings.toDomain())
        .build();
  }

  public static UserSettingsEntity fromDomain(UserSettings userSettings) {
    return UserSettingsEntity.builder()
        .version(CURRENT_VERSION)
        .id(userSettings.userSettingsId().id())
        .userId(userSettings.userId().id())
        .defaultLocation(LocationEntity.fromDomain(userSettings.defaultLocation()))
        .googleCalendarSettings(
            new GoogleCalendarSettingsEntity(
                userSettings.googleCalendarSettings().calendarId().id(),
                userSettings.googleCalendarSettings().synchroEnabled()))
        .build();
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
