package com.osslot.educorder.domain.repository;

import com.osslot.educorder.domain.model.UserSettings;
import java.util.List;
import java.util.Optional;

public interface UserSettingsRepository {

  Optional<UserSettings> findByUserId(String userId);

  List<UserSettings> findByGoogleCalendarSynchroEnabled(boolean synchroEnabled);

  void add(UserSettings userSettings);
}
