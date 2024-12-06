package com.osslot.educorder.domain.activities.model;

import com.osslot.educorder.domain.user.model.User.UserId;

public record ActivitySyncToken(ActivitySyncTokenId id, UserId userId, String syncToken) {

  public record ActivitySyncTokenId(String id) {}
}
