package com.osslot.educorder.infrastructure.activities.repository.entity;

import com.osslot.educorder.domain.activities.model.ActivitySyncToken;
import com.osslot.educorder.domain.user.model.User.UserId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ActivitySyncTokenEntity {

  private static final String CURRENT_VERSION = "0.0.1";

  public static final String PATH = "activities_sync_token";

  private String version;
  private String userId;
  private String syncToken;

  public ActivitySyncToken toDomain() {
    return new ActivitySyncToken(new UserId(userId), syncToken);
  }

  public static ActivitySyncTokenEntity fromDomain(ActivitySyncToken activitySyncToken) {
    return new ActivitySyncTokenEntity(
        CURRENT_VERSION, activitySyncToken.userId().id(), activitySyncToken.syncToken());
  }
}
