package com.osslot.educorder.domain.user.adapters;

import com.osslot.educorder.domain.activities.model.Location;
import com.osslot.educorder.domain.user.model.User;
import com.osslot.educorder.domain.user.model.UserSettings;
import com.osslot.educorder.domain.user.model.UserSettings.GoogleCalendarSettings.CalendarId;
import com.osslot.educorder.domain.user.repository.UserRepository;
import com.osslot.educorder.domain.user.repository.UserSettingsRepository;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class InitUser {

  private final UserRepository userRepository;
  private final UserSettingsRepository userSettingsRepository;

  public static final String CALENDAR_ID =
      "b32341848b6870ac8899d82601c990e3146d29a36cc404a6df2bfc6aa893c9ae@group.calendar.google.com";

  public void execute(User user) {
    if (userRepository.findByUserId(user.id()).isEmpty()) {
      userRepository.add(user);
    }
    if (userSettingsRepository.findByUserId(user.id()).isEmpty()) {
      userSettingsRepository.add(
          UserSettings.builder()
              .userSettingsId(
                  UserSettings.UserSettingsId.builder().id(UUID.randomUUID().toString()).build())
              .userId(user.id())
              .defaultLocation(new Location("13 rue saint vincent 33850 LEOGNAN"))
              .googleCalendarSettings(
                  UserSettings.GoogleCalendarSettings.builder()
                      .calendarId(new CalendarId(CALENDAR_ID))
                      .synchroEnabled(true)
                      .build())
              .build());
    }
  }
}
