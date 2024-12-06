package com.osslot.educorder.infrastructure.user.repository.entity;

import com.osslot.educorder.domain.user.model.User;
import com.osslot.educorder.infrastructure.common.repository.entity.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class UserEntity extends Entity {

  public static final String PATH = "users";
  private static final String CURRENT_VERSION = "0.0.1";

  private String name;
  private String email;

  public static UserEntity fromDomain(User user) {
    return UserEntity.builder()
        .id(user.id().id())
        .name(user.name())
        .email(user.email())
        .version(CURRENT_VERSION)
        .build();
  }

  public User toDomain() {
    return new User(new User.UserId(this.getId()), this.name, this.email);
  }
}
