package com.osslot.educorder.domain.repository;

import com.osslot.educorder.domain.model.ActivitySyncToken;
import com.osslot.educorder.domain.model.UserSettings.User;
import java.util.Optional;

public interface ActivitySyncTokenRepository {

  Optional<ActivitySyncToken> getCurrentActivitySyncToken(User user);

  void setCurrentActivitySyncToken(User user, ActivitySyncToken activitySyncToken);
}
