package com.osslot.educorder.infrastructure.repository.entity;

import com.osslot.educorder.domain.model.ActivitySyncToken;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ActivitySyncTokenEntity {

  public static final String PATH = "activities_sync_token";

  private String userId;
  private String syncToken;

  public ActivitySyncToken toDomain() {
    return new ActivitySyncToken(userId, syncToken);
  }

  public static ActivitySyncTokenEntity fromDomain(ActivitySyncToken activitySyncToken) {
    return new ActivitySyncTokenEntity(activitySyncToken.userId(), activitySyncToken.syncToken());
  }
}
