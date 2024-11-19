package com.osslot.educorder.application.activities.controller;

import com.osslot.educorder.application.mapper.UserMapper;
import com.osslot.educorder.domain.activities.adapters.CreateADIAPHKilometersFiles;
import com.osslot.educorder.domain.activities.adapters.CreateAPAJHKilometersFiles;
import com.osslot.educorder.domain.activities.model.Institution;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kilometers")
@AllArgsConstructor
public class KilometersController {

  private final CreateAPAJHKilometersFiles createAPAJHKilometersFiles;
  private final CreateADIAPHKilometersFiles createADIAPHKilometersFiles;

  @PostMapping("{institution}/{year}/{month}")
  public void createKilometers(
      @AuthenticationPrincipal Authentication authentication,
      @PathVariable Institution institution,
      @PathVariable int year,
      @PathVariable int month) {
    var user = UserMapper.fromAuthentication(authentication);
    switch (institution) {
      case APAJH -> createAPAJHKilometersFiles.execute(year, month);
      case ADIAPH -> createADIAPHKilometersFiles.execute(year, month);
      default -> throw new IllegalArgumentException("Institution not supported");
    }
  }

  @PostMapping("{institution}")
  public void createKilometers(
      @AuthenticationPrincipal Authentication authentication,
      @PathVariable Institution institution,
      @RequestBody KilometersRequest request) {
    var user = UserMapper.fromAuthentication(authentication);
    switch (institution) {
      case APAJH -> createAPAJHKilometersFiles.execute(user, request.start(), request.end());
      case ADIAPH -> createADIAPHKilometersFiles.execute(user, request.start(), request.end());
      default -> throw new IllegalArgumentException("Institution not supported");
    }
  }

  public record KilometersRequest(ZonedDateTime start, ZonedDateTime end) {}
}
