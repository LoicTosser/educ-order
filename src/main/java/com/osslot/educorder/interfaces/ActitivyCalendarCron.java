package com.osslot.educorder.interfaces;

import com.osslot.educorder.application.ImportActivitiesFromCalendar;
import java.time.ZonedDateTime;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ActitivyCalendarCron {

  private final ImportActivitiesFromCalendar importActivitiesFromCalendar;

  private String nextSyncToken;

  public ActitivyCalendarCron(ImportActivitiesFromCalendar importActivitiesFromCalendar) {
    this.importActivitiesFromCalendar = importActivitiesFromCalendar;
  }

  @Scheduled(cron = "0 */1 * * * *")
  public void syncActivities() {
    log.info("Syncing activities from calendar");
    if (nextSyncToken == null) {
      log.info("Sync token is null, no synchronization performed");
      return;
    }
    var fetchResult = importActivitiesFromCalendar.synchronize(nextSyncToken);
    nextSyncToken = fetchResult.nextSyncToken();
    log.info("Sync done, next sync token: {}", fetchResult.nextSyncToken());
  }

  @PostConstruct
  void init() {
    ZonedDateTime start = fromDateTime();
    var end = start.plusYears(1);
    var fetchResult =
        importActivitiesFromCalendar.importActivities(
                start, end);

    log.info("Initial import of activities done, next sync token: {}", fetchResult.nextSyncToken());
    nextSyncToken = fetchResult.nextSyncToken();
  }

  private static @NotNull ZonedDateTime fromDateTime() {
    return ZonedDateTime.of(2024, 10, 1, 0, 0, 0, 0, ZonedDateTime.now().getZone());
  }
}
