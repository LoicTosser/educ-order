package com.osslot.educorder.domain.user.model;

import lombok.Builder;

@Builder
public record User(UserId id, String name, String email) {
  public record UserId(String id) {}
}
