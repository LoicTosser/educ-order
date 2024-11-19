package com.osslot.educorder.domain.user.service;

import com.osslot.educorder.domain.model.User.UserId;
import com.osslot.educorder.domain.model.UserSettings;
import com.osslot.educorder.domain.user.adapters.UserSettingsAdapter;
import com.osslot.educorder.domain.user.repository.UserSettingsRepository;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserSettingsService implements UserSettingsAdapter {

  private final UserSettingsRepository userSettingsRepository;

  @Override
  public Optional<UserSettings> findByUserId(UserId userId) {
    return userSettingsRepository.findByUserId(userId);
  }

  @Override
  public List<UserSettings> findByGoogleCalendarSynchroEnabled(boolean synchroEnabled) {
    return userSettingsRepository.findByGoogleCalendarSynchroEnabled(synchroEnabled);
  }
}
