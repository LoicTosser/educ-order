package com.osslot.educorder.application.activities.controller;

import com.osslot.educorder.domain.activities.adapters.GetActivitiesSummaries;
import com.osslot.educorder.domain.activities.adapters.ImportActivitiesFromCalendar;
import com.osslot.educorder.domain.activities.adapters.ListActivities;
import com.osslot.educorder.domain.activities.model.Activity;
import com.osslot.educorder.domain.activities.model.ActivitySummaries;
import com.osslot.educorder.domain.user.adapters.AuthenticationService;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/activities")
@AllArgsConstructor
public class ActivitiesController {

  private final ImportActivitiesFromCalendar importActivitiesFromCalendar;
  private final GetActivitiesSummaries getActivitiesSummaries;
  private final ListActivities listActivities;
  private final AuthenticationService authenticationService;

  @PostMapping("{year}/{month}")
  public void importMonthActivities(@PathVariable int year, @PathVariable int month) {
    var user = authenticationService.getCurrentUser();
    importActivitiesFromCalendar.importActivities(user.id(), year, month);
  }

  @GetMapping()
  public List<Activity> listActivities(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @NonNull
          ZonedDateTime start,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam @NonNull
          ZonedDateTime end) {
    var user = authenticationService.getCurrentUser();
    return listActivities.listActivities(user.id(), start, end);
  }

  @GetMapping("/summary")
  public ActivitySummaries getActivitySummaries(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @NonNull
          ZonedDateTime start,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam @NonNull
          ZonedDateTime end) {
    var user = authenticationService.getCurrentUser();
    return getActivitiesSummaries.getActivitiesSummaries(user.id(), start, end);
  }

  public record ImportActivitiesRequest(@NonNull ZonedDateTime start, @NonNull ZonedDateTime end) {}
}
