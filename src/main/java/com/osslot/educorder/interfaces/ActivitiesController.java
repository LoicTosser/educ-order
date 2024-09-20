package com.osslot.educorder.interfaces;

import com.osslot.educorder.application.ImportActivitiesFromCalendar;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/activities")
@AllArgsConstructor
public class ActivitiesController {

  private final ImportActivitiesFromCalendar importActivitiesFromCalendar;

  @PostMapping("{year}/{month}")
  public void importMonthActivities(@PathVariable int year, @PathVariable int month) {
    importActivitiesFromCalendar.importActivities(year, month);
  }

  @PostMapping()
  public void importActivities(@RequestBody @Validated ImportActivitiesRequest request) {
    importActivitiesFromCalendar.importActivities(request.start(), request.end());
  }

  public record ImportActivitiesRequest(@NonNull ZonedDateTime start, @NonNull ZonedDateTime end) {}
}
