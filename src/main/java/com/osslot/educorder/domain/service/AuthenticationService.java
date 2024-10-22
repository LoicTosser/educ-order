package com.osslot.educorder.domain.service;

import com.osslot.educorder.domain.model.UserSettings.User;

public interface AuthenticationService {

  User getCurrentUser();
}
