package com.osslot.educorder.domain.activities.model;

import com.osslot.educorder.domain.model.User.UserId;

public record ActivitySyncToken(UserId userId, String syncToken) {}
