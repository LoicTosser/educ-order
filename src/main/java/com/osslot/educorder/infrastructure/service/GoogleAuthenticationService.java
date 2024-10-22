package com.osslot.educorder.infrastructure.service;

import com.osslot.educorder.domain.model.UserSettings.User;
import com.osslot.educorder.domain.service.AuthenticationService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GoogleAuthenticationService implements AuthenticationService {

  private ClientRegistrationRepository clientRegistrationRepository;

  @Override
  public User getCurrentUser() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication.getPrincipal() instanceof DefaultOAuth2User userDetails) {
      return new User(
          userDetails.getName(),
          userDetails.getAttribute("email"),
          userDetails.getAttribute("email"));
    }
    return null;
  }

  public void getAccessToken() {
    var clientRegistration = clientRegistrationRepository.findByRegistrationId("google");
  }
}
