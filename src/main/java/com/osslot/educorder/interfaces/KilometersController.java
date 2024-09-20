package com.osslot.educorder.interfaces;

import com.osslot.educorder.application.CreateADIAPHKilometersFiles;
import com.osslot.educorder.application.CreateAPAJHKilometersFiles;
import com.osslot.educorder.domain.model.Institution;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;

@RestController
@RequestMapping("/kilometers")
@AllArgsConstructor
public class KilometersController {

  private final CreateAPAJHKilometersFiles createAPAJHKilometersFiles;
  private final CreateADIAPHKilometersFiles createADIAPHKilometersFiles;

  @PostMapping("{institution}/{year}/{month}")
  public void createKilometers(
      @PathVariable Institution institution, @PathVariable int year, @PathVariable int month) {
    switch (institution) {
      case APAJH -> createAPAJHKilometersFiles.execute(year, month);
      case ADIAPH -> createADIAPHKilometersFiles.execute(year, month);
      default -> throw new IllegalArgumentException("Institution not supported");
    }
  }

  @PostMapping("{institution}")
  public void createKilometers(
          @PathVariable Institution institution, @RequestBody KilometersRequest request) {
    switch (institution) {
      case APAJH -> createAPAJHKilometersFiles.execute(request.start(), request.end());
      case ADIAPH -> createADIAPHKilometersFiles.execute(request.start(), request.end());
      default -> throw new IllegalArgumentException("Institution not supported");
    }
  }

  public record KilometersRequest(ZonedDateTime start, ZonedDateTime end) {}
}
