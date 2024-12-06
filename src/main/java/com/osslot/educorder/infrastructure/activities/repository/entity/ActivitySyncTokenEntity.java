package com.osslot.educorder.infrastructure.activities.repository.entity;

import com.osslot.educorder.domain.activities.model.ActivitySyncToken;
import com.osslot.educorder.domain.user.model.User.UserId;
import com.osslot.educorder.infrastructure.common.repository.entity.MultiTenantEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ActivitySyncTokenEntity extends MultiTenantEntity {

  private static final String CURRENT_VERSION = "0.0.1";

  public static final String PATH = "activities_sync_token";

  private String syncToken;

  public ActivitySyncToken toDomain() {
    return new ActivitySyncToken(
        new ActivitySyncToken.ActivitySyncTokenId(getId()), new UserId(getUserId()), syncToken);
  }

  public static ActivitySyncTokenEntity fromDomain(ActivitySyncToken activitySyncToken) {
    return ActivitySyncTokenEntity.builder()
        .version(CURRENT_VERSION)
        .userId(activitySyncToken.userId().id())
        .syncToken(activitySyncToken.syncToken())
        .build();
  }
}
