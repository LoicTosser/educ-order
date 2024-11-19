package com.osslot.educorder.infrastructure.activities.repository.entity;

import com.osslot.educorder.domain.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

  private String id;
  private String name;
  private String email;

  public static UserEntity fromDomain(User user) {
    return new UserEntity(user.id().id(), user.name(), user.email());
  }

  public User toDomain() {
    return new User(new User.UserId(this.id), this.name, this.email);
  }
}
