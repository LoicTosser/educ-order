package com.osslot.educorder.domain.user.repository;

import com.osslot.educorder.domain.user.model.User;
import java.util.Optional;

public interface UserRepository {
  Optional<User> findByUserId(User.UserId userId);

  void add(User user);
}
