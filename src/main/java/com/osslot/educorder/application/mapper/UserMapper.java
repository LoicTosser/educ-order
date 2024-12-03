package com.osslot.educorder.application.mapper;

import com.osslot.educorder.domain.user.model.User;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

@UtilityClass
public class UserMapper {

  public static User fromAuthentication(Authentication authentication) {
    if (authentication.getPrincipal() instanceof DefaultOAuth2User userDetails) {
      return new User(
          new User.UserId(userDetails.getName()),
          userDetails.getAttribute("email"),
          userDetails.getAttribute("email"));
    }
    return null;
  }
}
