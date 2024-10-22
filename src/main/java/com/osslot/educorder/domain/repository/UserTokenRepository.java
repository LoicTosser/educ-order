package com.osslot.educorder.domain.repository;

import com.osslot.educorder.domain.model.UserSettings.User;

public interface UserTokenRepository {

  void addToken(User user, String token);
}
