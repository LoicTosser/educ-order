package com.osslot.educorder.domain.user.repository;

import com.osslot.educorder.domain.user.model.User.UserId;
import com.osslot.educorder.domain.user.model.UserSettings;
import java.util.List;
import java.util.Optional;

public interface UserSettingsRepository {

  Optional<UserSettings> findByUserId(UserId userId);

  List<UserSettings> findByGoogleCalendarSynchroEnabled(boolean synchroEnabled);

  void add(UserSettings userSettings);
}
