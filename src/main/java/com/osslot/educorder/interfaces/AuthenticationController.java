package com.osslot.educorder.interfaces;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

  @GetMapping("/status")
  public ResponseEntity<Boolean> isAuthenticated(Authentication authentication) {
    return ResponseEntity.ok(authentication != null && authentication.isAuthenticated());
  }
}
