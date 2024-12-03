package com.osslot.educorder.domain.activities.repository;

import com.osslot.educorder.domain.activities.model.ActivitySyncToken;
import com.osslot.educorder.domain.user.model.User.UserId;
import java.util.Optional;

public interface ActivitySyncTokenRepository {

  Optional<ActivitySyncToken> getCurrentActivitySyncToken(UserId userId);

  void setCurrentActivitySyncToken(UserId userId, ActivitySyncToken activitySyncToken);
}
