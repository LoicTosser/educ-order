package com.osslot.educorder.domain.user.adapters;

import com.osslot.educorder.domain.model.User;
import com.osslot.educorder.domain.model.UserSettings;
import com.osslot.educorder.domain.model.UserSettings.GoogleCalendarSettings.CalendarId;
import com.osslot.educorder.domain.user.repository.UserSettingsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class InitUser {

  private final UserSettingsRepository userSettingsRepository;

  public static final String CALENDAR_ID =
      "b32341848b6870ac8899d82601c990e3146d29a36cc404a6df2bfc6aa893c9ae@group.calendar.google.com";

  public void execute(User user) {
    if (userSettingsRepository.findByUserId(user.id()).isEmpty()) {
      userSettingsRepository.add(
          UserSettings.builder()
              .userId(user.id())
              .googleCalendarSettings(
                  UserSettings.GoogleCalendarSettings.builder()
                      .calendarId(new CalendarId(CALENDAR_ID))
                      .synchroEnabled(true)
                      .build())
              .build());
    }
  }
}
