package com.osslot.educorder.interfaces;

import com.osslot.educorder.application.ImportActivitiesFromCalendar;
import com.osslot.educorder.application.ListActivities;
import com.osslot.educorder.domain.model.Activity;
import com.osslot.educorder.domain.service.AuthenticationService;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/activities")
@AllArgsConstructor
public class ActivitiesController {

  private final ImportActivitiesFromCalendar importActivitiesFromCalendar;
  private final ListActivities listActivities;
  private final AuthenticationService authenticationService;

  @PostMapping("{year}/{month}")
  public void importMonthActivities(@PathVariable int year, @PathVariable int month) {
    var user = authenticationService.getCurrentUser();
    importActivitiesFromCalendar.importActivities(user.id(), year, month);
  }

  @PostMapping()
  public void importActivities(@RequestBody @Validated ImportActivitiesRequest request) {
    var user = authenticationService.getCurrentUser();
    importActivitiesFromCalendar.importActivities(user.id(), request.start(), request.end());
  }

  @GetMapping()
  public List<Activity> listActivities(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @NonNull
          ZonedDateTime start,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam @NonNull
          ZonedDateTime end) {
    var user = authenticationService.getCurrentUser();
    return listActivities.listActivities(start, end);
  }

  public record ImportActivitiesRequest(@NonNull ZonedDateTime start, @NonNull ZonedDateTime end) {}
}
