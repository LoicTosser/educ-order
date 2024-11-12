package com.osslot.educorder.interfaces;

import com.osslot.educorder.application.InitUser;
import com.osslot.educorder.interfaces.mapper.UserMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class AuthenticationEvents {

  private final InitUser initUser;

  @EventListener
  public void onSuccess(AuthenticationSuccessEvent success) {
    log.info("User authenticated {}", success.getAuthentication());
    var user = UserMapper.fromAuthentication(success.getAuthentication());
    initUser.execute(user);
  }

  @EventListener
  public void onFailure(AbstractAuthenticationFailureEvent failures) {}
}