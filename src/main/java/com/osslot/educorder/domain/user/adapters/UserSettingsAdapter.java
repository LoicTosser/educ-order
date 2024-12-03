package com.osslot.educorder.domain.user.adapters;

import com.osslot.educorder.domain.user.model.User.UserId;
import com.osslot.educorder.domain.user.model.UserSettings;
import java.util.List;
import java.util.Optional;

public interface UserSettingsAdapter {

  Optional<UserSettings> findByUserId(UserId userId);

  List<UserSettings> findByGoogleCalendarSynchroEnabled(boolean synchroEnabled);
}
