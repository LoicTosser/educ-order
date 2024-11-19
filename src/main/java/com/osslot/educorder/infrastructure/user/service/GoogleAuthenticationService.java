package com.osslot.educorder.infrastructure.user.service;

import com.osslot.educorder.domain.model.User;
import com.osslot.educorder.domain.model.User.UserId;
import com.osslot.educorder.domain.user.adapters.AuthenticationService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GoogleAuthenticationService implements AuthenticationService {

  @Override
  public User getCurrentUser() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication.getPrincipal() instanceof DefaultOAuth2User userDetails) {
      return new User(
          new UserId(userDetails.getName()),
          userDetails.getAttribute("email"),
          userDetails.getAttribute("email"));
    }
    return null;
  }
}
