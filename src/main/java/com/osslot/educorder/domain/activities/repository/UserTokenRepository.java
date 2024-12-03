package com.osslot.educorder.domain.activities.repository;

import com.osslot.educorder.domain.user.model.User;

public interface UserTokenRepository {

  void addToken(User user, String token);
}
