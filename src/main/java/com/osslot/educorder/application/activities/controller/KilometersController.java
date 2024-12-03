package com.osslot.educorder.application.activities.controller;

import com.osslot.educorder.domain.activities.adapters.CreateKilometersFiles;
import com.osslot.educorder.domain.activities.model.Institution;
import com.osslot.educorder.domain.user.adapters.AuthenticationService;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kilometers")
@AllArgsConstructor
public class KilometersController {

  private final CreateKilometersFiles createKilometersFiles;
  private final AuthenticationService authenticationService;

  @PostMapping("{institution}")
  public void createKilometers(
      @PathVariable Institution institution, @RequestBody KilometersRequest request) {
    var user = authenticationService.getCurrentUser();
    switch (institution) {
      case APAJH ->
          createKilometersFiles.createAphjhKilometersFiles(user, request.start(), request.end());
      case ADIAPH ->
          createKilometersFiles.createAdiaphKilometersFiles(user, request.start(), request.end());
      default -> throw new IllegalArgumentException("Institution not supported");
    }
  }

  public record KilometersRequest(ZonedDateTime start, ZonedDateTime end) {}
}
